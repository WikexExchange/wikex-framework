package com.wikex.wikex.market.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ExchangeOrderDirection;
import com.wikex.wikex.market.handler.NettyHandler;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.pojo.TradePlate;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ExchangePushJob {
    private Logger logger = LoggerFactory.getLogger(ExchangePushJob.class);
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    @Value("${platformCoins}")
    private String platformCoins;
    @Autowired
    private NettyHandler nettyHandler;
    private Map<String, List<ExchangeTrade>> tradesQueue = new HashMap<>();
    private Map<String, List<TradePlate>> plateQueue = new HashMap<>();
    private Map<String, List<CoinThumb>> thumbQueue = new HashMap<>();

    public void addTrades(String symbol, List<ExchangeTrade> trades) {
        List<ExchangeTrade> list = tradesQueue.get(symbol);
        if (list == null) {
            list = new ArrayList<>();
            tradesQueue.put(symbol, list);
        }
        synchronized (list) {
            list.addAll(trades);
        }
    }

    public void addPlates(String symbol, TradePlate plate) {
        List<TradePlate> list = plateQueue.get(symbol);
        if (list == null) {
            list = new ArrayList<>();
            plateQueue.put(symbol, list);
        }
        synchronized (list) {
            list.add(plate);
        }
    }

    public void addThumb(String symbol, CoinThumb thumb) {
        List<CoinThumb> list = thumbQueue.get(symbol);
        if (list == null) {
            list = new ArrayList<>();
            thumbQueue.put(symbol, list);
        }
        synchronized (list) {
            list.add(thumb);
        }
    }

    @XxlJob("pushTrade")
    public void pushTrade() {
        Iterator<Map.Entry<String, List<ExchangeTrade>>> entryIterator = tradesQueue.entrySet().iterator();
        while (entryIterator.hasNext()) {
            try {
                Map.Entry<String, List<ExchangeTrade>> entry = entryIterator.next();
                String symbol = entry.getKey();
                List<ExchangeTrade> trades = entry.getValue();

                if (trades.size() > 0) {
                    synchronized (trades) {
                        messagingTemplate.convertAndSend("/topic/market/trade/" + symbol, trades);
                        Map<String, Object> map = new HashMap<>();
                        map.put("symbol", symbol);
                        map.put("trades", trades);
                        rocketMQTemplate.convertAndSend("market-trade", JSON.toJSONString(map));
                        trades.clear();
                    }
                }
            } catch (Exception e) {
                logger.error("Error pushTrade", e);
            }
        }
    }

    @XxlJob("pushPlate")
    public void pushPlate() {
        Iterator<Map.Entry<String, List<TradePlate>>> entryIterator = plateQueue.entrySet().iterator();
        while (entryIterator.hasNext()) {
            try {
                Map.Entry<String, List<TradePlate>> entry = entryIterator.next();
                String symbol = entry.getKey();
                List<TradePlate> plates = entry.getValue();
                if (plates.size() > 0) {
                    boolean hasPushAskPlate = false;
                    boolean hasPushBidPlate = false;
                    synchronized (plates) {
                        for (TradePlate plate : plates) {
                            if (plate.getDirection().getCode() == ExchangeOrderDirection.BUY.getCode() && !hasPushBidPlate) {
                                hasPushBidPlate = true;
                            } else if (plate.getDirection().getCode() == ExchangeOrderDirection.SELL.getCode() && !hasPushAskPlate) {
                                hasPushAskPlate = true;
                            } else {
                                continue;
                            }
                            JSONObject json = plate.toJSON(50);
                            // websocket push order book info
                            messagingTemplate.convertAndSend("/topic/market/trade-plate/" + symbol, json);
                            // websocket push depth info
                            messagingTemplate.convertAndSend("/topic/market/trade-depth/" + symbol, json);
                            // netty push
                            nettyHandler.handlePlate(symbol, plate);
                            // push data, only push for platform coins
                            String[] split = symbol.split("/");
                            if (platformCoins.contains(split[0]) || platformCoins.contains(split[1])) {
                                Map<String, Object> map = new HashMap<>();
                                map.put("symbol", symbol);
                                map.put("plates", json);
                                rocketMQTemplate.convertAndSend("market-trade-plate", JSON.toJSONString(map));
                            }
                        }

                        plates.clear();
                    }
                }
            } catch (Exception e){
                logger.error("Error pushPlate", e);
            }
        }
    }

    // @Scheduled(fixedRate = 300)
    @XxlJob("pushThumb")
    public void pushThumb() {
        Iterator<Map.Entry<String, List<CoinThumb>>> entryIterator = thumbQueue.entrySet().iterator();
        while (entryIterator.hasNext()) {
            try {
                Map.Entry<String, List<CoinThumb>> entry = entryIterator.next();
                // String symbol = entry.getKey();
                List<CoinThumb> thumbs = entry.getValue();
                if (thumbs.size() > 0) {
                    synchronized (thumbs) {
                        messagingTemplate.convertAndSend("/topic/market/thumb", thumbs.get(thumbs.size() - 1));
                        rocketMQTemplate.convertAndSend("market-thumb", JSON.toJSONString(thumbs.get(thumbs.size() - 1)));
                        thumbs.clear();
                    }
                }
            } catch (Exception e) {
                logger.error("Error pushThumb", e);
            }
        }
    }
}