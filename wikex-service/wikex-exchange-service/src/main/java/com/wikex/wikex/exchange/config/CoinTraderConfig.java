package com.wikex.wikex.exchange.config;

import com.wikex.wikex.constant.ExchangeCoinPublishType;
import com.wikex.wikex.exchange.Trader.CoinTrader;
import com.wikex.wikex.exchange.Trader.CoinTraderFactory;
import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.service.ExchangeCoinService;
import com.wikex.wikex.exchange.service.ExchangeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
public class CoinTraderConfig {

    
    @Bean
    public CoinTraderFactory getCoinTrader(ExchangeCoinService exchangeCoinService, RocketMQTemplate rocketMQTemplate, ExchangeOrderService exchangeOrderService){
        CoinTraderFactory factory = new CoinTraderFactory();
        List<ExchangeCoin> coins = exchangeCoinService.findAllEnabled();
        for(ExchangeCoin coin:coins) {
            
            CoinTrader trader = new CoinTrader(coin.getSymbol());
            trader.setRocketMQTemplate(rocketMQTemplate);
            trader.setBaseCoinScale(coin.getBaseCoinScale());
            trader.setCoinScale(coin.getCoinScale());
            trader.setPublishType(ExchangeCoinPublishType.creator(coin.getPublishType()));
            trader.setClearTime(coin.getClearTime());
            trader.stopTrading();
            factory.addTrader(coin.getSymbol(),trader);
        }
        return factory;
    }

}
