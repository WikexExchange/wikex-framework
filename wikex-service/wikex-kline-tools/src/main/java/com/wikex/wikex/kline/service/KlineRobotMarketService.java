package com.wikex.wikex.kline.service;

import com.wikex.wikex.kline.entity.KLine;
import com.wikex.wikex.kline.entity.Symbol;

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
