package com.wikex.wikex.p2p.config;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;

@Slf4j
public class CoinExchangeFactory {
    @Setter
    private HashMap<String,HashMap<String,BigDecimal>> coins;

    public HashMap<String,HashMap<String,BigDecimal>> getCoins(){
        return coins;
    }

    public CoinExchangeFactory(){
        coins = new HashMap<>();
    }

    public BigDecimal get(String symbol,String currency){
        return coins.get(symbol).get(currency);
    }

    
    public void  set(String symbol,BigDecimal rate,String currency){
        HashMap<String, BigDecimal> currencyMap = coins.get(symbol);
        if(currencyMap==null){
            currencyMap = new HashMap<>();
            coins.put(symbol,currencyMap);
        }
        currencyMap.put("CNY",rate);
        coins.put(symbol,currencyMap);
    }

    public void set(String symbol,HashMap<String, BigDecimal> currencyMap){
        coins.put(symbol,currencyMap);
        
    }
}
