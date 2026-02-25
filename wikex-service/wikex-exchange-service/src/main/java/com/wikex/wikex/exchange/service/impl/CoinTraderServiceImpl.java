package com.wikex.wikex.exchange.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.exchange.config.TradingConfig;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.feign.MatchFeign;
import com.wikex.wikex.exchange.service.CoinTraderService;
import com.wikex.wikex.pojo.TradePlateItem;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CoinTraderServiceImpl implements CoinTraderService {

    @Autowired
    private MatchFeign matchFeign;
    @Autowired
    private TradingConfig tradingConfig;

    @Override
    public Boolean containsTrader(String symbol) {
        String serviceName =  tradingConfig.getServiceName(symbol);
        return matchFeign.containsTrader(serviceName,symbol);
    }

    @Override
    public void resetTrader(String symbol) {
        String serviceName =  tradingConfig.getServiceName(symbol);
        matchFeign.resetTrader(serviceName,symbol);
    }

    @Override
    public void stopTrader(String symbol) {
        String serviceName =  tradingConfig.getServiceName(symbol);
        matchFeign.stopTrader(serviceName,symbol);
    }

    @Override
    public Map<String, Integer> engines() {
        Map<String, Integer> symbols = new HashMap<String, Integer>();
        try {
            Map<String, List<String>> pairs = tradingConfig.getPairs();
            Set<String> keySet = pairs.keySet();
            for (String serviceName : keySet) {
                Map<String, Integer> symbol = matchFeign.engines(serviceName);
                symbols.putAll(symbol);
            }
            if (StringUtils.isNotEmpty(tradingConfig.getServiceName())) {
                Map<String, Integer> symbol = matchFeign.engines(tradingConfig.getServiceName());
                symbols.putAll(symbol);
            }
            return symbols;
        } catch (Exception e) {
            return symbols;
        }
    }

    @Override
    public Map<String, List<TradePlateItem>> plate(String symbol) {
        String serviceName =  tradingConfig.getServiceName(symbol);
        return matchFeign.plate(serviceName,symbol);
    }

    @Override
    public Map<String, JSONObject> plateMini(String symbol) {
        try {
            String serviceName = tradingConfig.getServiceName(symbol);
            Map<String, Object> result = matchFeign.plateMini(serviceName, symbol);
            if (result == null || result.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, JSONObject> converted = new HashMap<>();
            for (Map.Entry<String, Object> entry : result.entrySet()) {
                if (entry.getValue() instanceof JSONObject) {
                    converted.put(entry.getKey(), (JSONObject) entry.getValue());
                } else {
                    converted.put(entry.getKey(), JSON.parseObject(JSON.toJSONString(entry.getValue())));
                }
            }
            return converted;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, JSONObject> plateFull(String symbol) {
        try {
            String serviceName = tradingConfig.getServiceName(symbol);
            Map<String, Object> result = matchFeign.plateFull(serviceName, symbol);
            if (result == null || result.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, JSONObject> converted = new HashMap<>();
            for (Map.Entry<String, Object> entry : result.entrySet()) {
                if (entry.getValue() instanceof JSONObject) {
                    converted.put(entry.getKey(), (JSONObject) entry.getValue());
                } else {
                    converted.put(entry.getKey(), JSON.parseObject(JSON.toJSONString(entry.getValue())));
                }
            }
            return converted;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public Boolean getTradingStatus(String symbol) {
        String serviceName = tradingConfig.getServiceName(symbol);
        return matchFeign.getTradingStatus(serviceName,symbol);
    }

    @Override
    public JSONObject traderOverview(String symbol) {
        String serviceName = tradingConfig.getServiceName(symbol);
        return matchFeign.traderOverview(serviceName,symbol);
    }

    @Override
    public MessageResult startTrader(String symbol) {
        String serviceName = tradingConfig.getServiceName(symbol);
        return matchFeign.startTrader(serviceName,symbol);
    }

    @Override
    public ExchangeOrder findOrder(String symbol, String orderId, Integer type, Integer direction) {
        String serviceName = tradingConfig.getServiceName(symbol);
        return matchFeign.findOrder(serviceName,symbol,orderId,type,direction);
    }

    @Override
    public JSONObject traderDetail(String symbol) {
        String serviceName = tradingConfig.getServiceName(symbol);
        return matchFeign.traderDetail(serviceName,symbol);
    }

    @Override
    public List<String> symbols() {
        List<String> result = new ArrayList<>();
        Map<String, List<String>> pairs = tradingConfig.getPairs();
        Set<String> keySet = pairs.keySet();
        for (String serviceName : keySet) {
            List<String> symbols = matchFeign.symbols(serviceName);
            result.addAll(symbols);
        }
        if(StringUtils.isNotEmpty(tradingConfig.getServiceName())){
            List<String> symbols = matchFeign.symbols(tradingConfig.getServiceName());
            result.addAll(symbols);
        }
        return result;
    }


}
