package com.wikex.wikex.swap.handler;

import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ContractTrade;
import com.wikex.wikex.pojo.KLine;

import java.util.List;

public interface MarketHandler {

    
    void handleTrade(String symbol, CoinThumb thumb);

    
    void handleTrades(String symbol, List<ContractTrade> contractTrades, CoinThumb thumb);

    
    void handleKLine(String symbol, KLine kLine);
}
