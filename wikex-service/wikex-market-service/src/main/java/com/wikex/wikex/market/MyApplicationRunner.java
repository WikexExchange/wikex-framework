package com.wikex.wikex.market;

import com.wikex.wikex.exchange.entity.ExchangeCoin;
import com.wikex.wikex.exchange.feign.ExchangeCoinFeign;
import com.wikex.wikex.market.processor.CoinProcessor;
import com.wikex.wikex.market.processor.CoinProcessorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class MyApplicationRunner implements ApplicationRunner {
    @Autowired
    private CoinProcessorFactory coinProcessorFactory;
    @Autowired
    private ExchangeCoinFeign coinService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if(coinService==null){
            log.warn("ExchangeCoinFeign service is null, skipping initialization");
            return;
        }
        try {
            List<ExchangeCoin> coins = coinService.findAllEnabled();
            if (coins == null || coins.isEmpty()) {
                log.warn("No enabled coins found, skipping processor initialization");
                return;
            }
            coins.forEach(coin->{
                try {
                    CoinProcessor processor = coinProcessorFactory.getProcessor(coin.getSymbol());
                    if (processor == null) {
                        log.warn("Processor not found for symbol: {}", coin.getSymbol());
                        return;
                    }
                    processor.initializeThumb();
                    processor.initializeUsdRate();
                    processor.setIsHalt(false);
                } catch (Exception e) {
                    log.error("Failed to initialize processor for symbol: {}", coin.getSymbol(), e);
                }
            });
        } catch (Exception e) {
            log.error("Critical error during application startup initialization", e);
        }
    }
}
