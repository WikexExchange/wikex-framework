package com.wikex.wikex.swap.engine;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ContractCoinMatchFactory {

    // symbol, match
    private ConcurrentHashMap<String, ContractCoinMatch> matchMap;

    public ContractCoinMatchFactory(){
        this.matchMap = new ConcurrentHashMap<>();
    }

    public void addContractCoinMatch(String symbol, ContractCoinMatch match) {
        
        if(!this.containsContractCoinMatch(symbol)) {
            this.matchMap.put(symbol, match);
        }
    }

    public boolean containsContractCoinMatch(String symbol) {
        return this.matchMap != null && this.matchMap.containsKey(symbol);
    }

    public ContractCoinMatch getContractCoinMatch(String symbol) {
        return this.matchMap.get(symbol);
    }

    public Map<String,ContractCoinMatch > getMatchMap() {
        return this.matchMap;
    }

    public void refreshPrice(String symbol,BigDecimal newPrice) {
        Map<String,BigDecimal> mapPrice = new HashMap<>();
        for (ContractCoinMatch value : this.matchMap.values()) {
            mapPrice.put(value.getSymbol(),value.getNowPrice());
        }
        mapPrice.put(symbol,newPrice);
        this.getContractCoinMatch(symbol).refreshPrice(mapPrice);
    }
}
