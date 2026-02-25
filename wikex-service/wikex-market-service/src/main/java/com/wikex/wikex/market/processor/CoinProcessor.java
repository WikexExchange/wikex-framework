package com.wikex.wikex.market.processor;


import com.wikex.wikex.market.component.CoinExchangeRate;
import com.wikex.wikex.market.handler.MarketHandler;
import com.wikex.wikex.market.service.MarketService;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.pojo.KLine;

import java.util.List;

public interface CoinProcessor {

    void setIsHalt(boolean status);

    void setIsStopKLine(boolean stop);
    
    boolean isStopKline();
    /**
     * @param trades
     * @return
     */
    void process(List<ExchangeTrade> trades);

    /**
     * @param storage
     */
    void addHandler(MarketHandler storage);

    CoinThumb getThumb();

    void setMarketService(MarketService service);

    void generateKLine(int range, int field, long time);

    void generateKLine1min(int range, int field, long time);

    KLine getKLine();

    void initializeThumb();

    void autoGenerate();

    void resetThumb();

    void setExchangeRate(CoinExchangeRate coinExchangeRate);

    void update24HVolume(long time);

    void initializeUsdRate();

    void generateKLine(long time, int minute, int hour);
}
