package com.wikex.wikex.market.config;

import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.feign.ExchangeCoinFeign;
import com.wikex.wikex.market.component.CoinExchangeRate;
import com.wikex.wikex.market.handler.MongoMarketHandler;
import com.wikex.wikex.market.handler.NettyHandler;
import com.wikex.wikex.market.handler.WebsocketMarketHandler;
import com.wikex.wikex.market.processor.CoinProcessor;
import com.wikex.wikex.market.processor.CoinProcessorFactory;
import com.wikex.wikex.market.processor.DefaultCoinProcessor;
import com.wikex.wikex.market.service.MarketService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

@Configuration
@Slf4j
public class ProcessorConfig {
    private Logger logger = LoggerFactory.getLogger(ContractCoinMatchStarter.class);
    @Bean
    public CoinProcessorFactory processorFactory(MongoMarketHandler mongoMarketHandler,
                                                 @Lazy WebsocketMarketHandler wsHandler,
                                                 NettyHandler nettyHandler,
                                                 MarketService marketService,
                                                 CoinExchangeRate exchangeRate,
                                                 ExchangeCoinFeign exchangeCoinFeign) {
        CoinProcessorFactory factory = new CoinProcessorFactory();
        try {
            List<ExchangeCoin> coins = exchangeCoinFeign.findAllEnabled();
            if (coins == null || coins.isEmpty()) {
                exchangeRate.setCoinProcessorFactory(factory);
                return factory;
            }

            for (ExchangeCoin coin : coins) {
                try {
                    CoinProcessor processor = new DefaultCoinProcessor(coin.getSymbol(), coin.getBaseSymbol());
                    processor.addHandler(mongoMarketHandler);
                    processor.addHandler(wsHandler);
                    processor.addHandler(nettyHandler);
                    processor.setMarketService(marketService);
                    processor.setExchangeRate(exchangeRate);
                    processor.setIsStopKLine(true);
                    
                    factory.addProcessor(coin.getSymbol(), processor);
                } catch (Exception e) {
                    logger.error("Failed to create processor for symbol: {}", coin.getSymbol(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Critical error during processor factory initialization", e);
        }
        exchangeRate.setCoinProcessorFactory(factory);
        return factory;
    }
}
