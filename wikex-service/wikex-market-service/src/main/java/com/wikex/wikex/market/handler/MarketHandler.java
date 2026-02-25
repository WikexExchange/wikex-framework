package com.wikex.wikex.market.handler;

import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.pojo.KLine;

public interface MarketHandler {

    /**
     * Store trade information
     * @param exchangeTrade
     */
    void handleTrade(String symbol, ExchangeTrade exchangeTrade, CoinThumb thumb);

    /**
     * Store K-line information
     * @param kLine
     */
    void handleKLine(String symbol, KLine kLine);
}
