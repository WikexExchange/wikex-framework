package com.wikex.wikex.user.task;

import com.wikex.wikex.market.feign.ExchangeRateFeign;
import com.wikex.wikex.user.system.CoinExchangeFactory;
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

    @XxlJob("userSyncRate")
    public void userSyncRate() {
        
        factory.getCoins().forEach(
                (symbol, value) -> {
                    try{
                        HashMap<String,BigDecimal> rates = exchangeRateFeign.getAllExchangeRate(symbol);
                        
                        BigDecimal usdRate = rates.get("USD");
                        BigDecimal cnyRate = rates.get("CNY");
                        factory.set(symbol, usdRate,cnyRate);
                    } catch (Exception e){
                        e.printStackTrace();
                        log.error("unit = {} ,get rate error ! default value zero!", symbol);
                    }
                });
        
    }

}
