package com.wikex.wikex.market.job;

import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.feign.ExchangeCoinFeign;
import com.wikex.wikex.exchange.feign.MonitorFeign;
import com.wikex.wikex.market.component.CoinExchangeRate;
import com.wikex.wikex.market.handler.MongoMarketHandler;
import com.wikex.wikex.market.handler.NettyHandler;
import com.wikex.wikex.market.handler.WebsocketMarketHandler;
import com.wikex.wikex.market.processor.CoinProcessor;
import com.wikex.wikex.market.processor.CoinProcessorFactory;
import com.wikex.wikex.market.processor.DefaultCoinProcessor;
import com.wikex.wikex.market.service.MarketService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Automatically synchronize trading pairs from the Exchange matching trading center
 *
 */
@Component
@Slf4j
public class CoinProcessorJob {
    private Logger logger = LoggerFactory.getLogger(CoinProcessorJob.class);
	@Autowired
    private CoinProcessorFactory processorFactory;
    @Autowired
    private ExchangeCoinFeign coinService;

    @Autowired
    MongoMarketHandler mongoMarketHandler;

    @Autowired
    WebsocketMarketHandler wsHandler;

    @Autowired
    NettyHandler nettyHandler;

    @Autowired
    MarketService marketService;

    @Autowired
    CoinExchangeRate exchangeRate;
    @Autowired
    private MonitorFeign monitorFeign;

    /**
     * 1-minute timer, runs every 1 minute
     */

    @XxlJob("synchronizeExchangeCenter")
    public void synchronizeExchangeCenter(){
    	try {

            Map<String, Integer> exchangeCenterCoins = monitorFeign.engines();

            Map<String, CoinProcessor> processorMap = processorFactory.getProcessorMap();


            for (Map.Entry<String, Integer> coin : exchangeCenterCoins.entrySet()) {
                String symbol = coin.getKey();
                Integer status = coin.getValue();

                if (processorMap.containsKey(symbol)) {
                    CoinProcessor temProcessor = processorMap.get(symbol);
                    if (status.intValue() == 1) {

                        if (temProcessor.isStopKline()) {
                            temProcessor.setIsStopKLine(false);

                        }
                    } else if (status.intValue() == 2) {

                        if (!temProcessor.isStopKline()) {

                            temProcessor.setIsStopKLine(true);
                        }
                    }
                    continue;
                }


                ExchangeCoin focusCoin = coinService.findBySymbol(symbol);
                if (focusCoin == null) {
                    continue;
                }


                CoinProcessor processor = new DefaultCoinProcessor(symbol, focusCoin.getBaseSymbol());
                processor.addHandler(mongoMarketHandler);
                processor.addHandler(wsHandler);
                processor.addHandler(nettyHandler);
                processor.setMarketService(marketService);
                processor.setExchangeRate(exchangeRate);
                processor.initializeThumb();
                processor.initializeUsdRate();
                processor.setIsHalt(false);

                if (status.intValue() == 2) {
                    processor.setIsStopKLine(true);
                }
                processorFactory.addProcessor(symbol, processor);


            }
        } catch (Exception e) {
            logger.error("Error synchronizeExchangeCenter", e);
        }
    }
}
