package com.wikex.wikex.user.config;

import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.service.CoinService;
import com.wikex.wikex.user.system.CoinExchangeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CoinExchangeFactoryConfig {
    @Autowired
    private CoinService coinService;

    @Bean
    public CoinExchangeFactory createCoinExchangeFactory() {
        List<Coin> coinList = coinService.list();
        CoinExchangeFactory factory = new CoinExchangeFactory();
        coinList.forEach(coin ->
                factory.set(coin.getUnit(), coin.getUsdRate(), coin.getCnyRate())
        );
        return factory;
    }
}
