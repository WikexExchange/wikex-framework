package com.wikex.wikex.exchange.service;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.pojo.TradePlateItem;
import com.wikex.wikex.util.MessageResult;

import java.util.List;
import java.util.Map;

public interface CoinTraderService {
    Boolean containsTrader(String symbol);

    void resetTrader(String symbol);

    void stopTrader(String symbol);

    Map<String, Integer> engines();

    Map<String, List<TradePlateItem>> plate(String symbol);

    Map<String, JSONObject> plateMini(String symbol);

    Map<String, JSONObject> plateFull(String symbol);

    Boolean getTradingStatus(String symbol);

    JSONObject traderOverview(String symbol);

    MessageResult startTrader(String symbol);

    ExchangeOrder findOrder(String symbol, String orderId, Integer type, Integer direction);

    JSONObject traderDetail(String symbol);

    List<String> symbols();
}
