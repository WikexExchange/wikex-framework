package com.wikex.wikex.swap.handler;

import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ContractTrade;
import com.wikex.wikex.pojo.KLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoMarketHandler implements MarketHandler {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void handleTrade(String symbol, CoinThumb thumb) {
        // Get Huobi market data, no need to store
    }

    @Override
    public void handleTrades(String symbol, List<ContractTrade> contractTrades, CoinThumb thumb) {
        // Get Huobi market data, no need to store
    }

    @Override
    public void handleKLine(String symbol, KLine kLine) {
        // Get Huobi market data, no need to store
    }
}
