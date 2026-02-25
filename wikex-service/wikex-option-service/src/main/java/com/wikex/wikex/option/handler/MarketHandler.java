package com.wikex.wikex.option.handler;


import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.KLine;

public interface MarketHandler {

    
    void handleTrade(String symbol, CoinThumb thumb);


    
    void handleKLine(String symbol, KLine kLine);
}
