package com.wikex.wikex.p2p.config;

import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.p2p.service.OtcCoinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Configuration
@Slf4j
public class CoinExchangeFactoryConfig {

    @Bean
    public CoinExchangeFactory getCoinExchangeFactory(OtcCoinService coinService) {
        
        List<OtcCoin> coins = coinService.list();
        CoinExchangeFactory factory = new CoinExchangeFactory();
        HashMap<String,BigDecimal> ratesMap = new HashMap<String,BigDecimal>(){{
            put("CNY",new BigDecimal(0));
            put("TWD",new BigDecimal(0));
            put("USD",new BigDecimal(0));
            put("EUR",new BigDecimal(0));
            put("HKD",new BigDecimal(0));
            put("SGD",new BigDecimal(0));
        }};
        coins.forEach(coin -> {
            factory.set(coin.getUnit(), ratesMap);
            
        });
        factory.set("USDT", ratesMap);
        
        
        return factory;
    }
}
