package com.wikex.wikex.market.service;


import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.Symbol;

import java.util.List;

public interface KlineRobotMarketService {


    public void saveKLine(String symbol, KLine kLine);

    /**
     * @param symbol
     * @param period
     * @return
     */
    public long findMaxTimestamp(String symbol, String period);

    public List<Symbol> findAllSymbol();

    public void addSymbol(Symbol symbol);

    public void deleteAll(String symbol);
}
