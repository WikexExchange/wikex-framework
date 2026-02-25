package com.wikex.wikex.market.handler;

import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.pojo.KLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoMarketHandler implements MarketHandler {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void handleTrade(String symbol, ExchangeTrade exchangeTrade, CoinThumb thumb) {
        try {
            mongoTemplate.insert(exchangeTrade, "exchange_trade_" + symbol);
        } catch (Exception e) {
            // TODO
        }
    }

    @Override
    public void handleKLine(String symbol, KLine kLine) {
        try {
            mongoTemplate.insert(kLine, "exchange_kline_" + symbol + "_" + kLine.getPeriod());
        } catch (Exception e) {
            // TODO
        }
    }
}
