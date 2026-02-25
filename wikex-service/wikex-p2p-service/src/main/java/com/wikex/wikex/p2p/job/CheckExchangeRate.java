package com.wikex.wikex.p2p.job;

import com.wikex.wikex.market.feign.ExchangeRateFeign;
import com.wikex.wikex.p2p.config.CoinExchangeFactory;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;

@Component
@Slf4j
public class CheckExchangeRate {

    @Autowired
    private CoinExchangeFactory factory;
    @Autowired
    private ExchangeRateFeign exchangeRateFeign;

//    @Scheduled(fixedRate = 5 * 60 * 1000)
    @XxlJob("syncRate")
    public void syncRate() {
        
        factory.getCoins().forEach(
                (symbol, value) -> {
                    try{
                        HashMap<String,BigDecimal> rates = exchangeRateFeign.getAllExchangeRate(symbol);
                        
                        factory.set(symbol, rates);
                    } catch (Exception e){
                        e.printStackTrace();
                        log.error("unit = {} ,get rate error ! default value zero!", symbol);
                        factory.set(symbol, new HashMap<>());
                    }
                });
        
    }

}
