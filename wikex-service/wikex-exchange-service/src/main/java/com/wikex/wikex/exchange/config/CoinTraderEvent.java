package com.wikex.wikex.exchange.config;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.exchange.Trader.CoinTrader;
import com.wikex.wikex.exchange.Trader.CoinTraderFactory;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;
import com.wikex.wikex.exchange.service.ExchangeOrderDetailService;
import com.wikex.wikex.exchange.service.ExchangeOrderService;
import com.wikex.wikex.exchange.util.OrderUtils;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CoinTraderEvent implements ApplicationRunner {
    private Logger log = LoggerFactory.getLogger(CoinTraderEvent.class);
    @Autowired
    CoinTraderFactory coinTraderFactory;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private ExchangeOrderDetailService exchangeOrderDetailService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Override
    @Async
    public void run(ApplicationArguments args) throws Exception {
        Map<String, CoinTrader> traders = coinTraderFactory.getTraderMap();
        
        for(Map.Entry<String, CoinTrader> entry : traders.entrySet()) {
            String symbol = entry.getKey();
            CoinTrader trader = entry.getValue();
            
            try {
                List<ExchangeOrder> orders = exchangeOrderService.findAllTradingOrderBySymbol(symbol);
                
                if(orders == null || orders.isEmpty()) {
                    trader.setReady(true);
                    continue;
                }
                
                // Batch load all order details to avoid N+1 query problem, but limit each call to 100 ids
                List<String> orderIds = orders.stream()
                    .map(ExchangeOrder::getOrderId)
                    .collect(Collectors.toList());

                Map<String, ExchangeOrderDetail> detailsMap = new HashMap<>();
                for (List<String> chunk : chunkList(orderIds, 100)) {
                    Map<String, ExchangeOrderDetail> part = exchangeOrderDetailService.findAllByOrderIds(chunk);
                    if (part != null) {
                        detailsMap.putAll(part);
                    }
                }
                
                List<ExchangeOrder> tradingOrders = new ArrayList<>();
                List<ExchangeOrder> completedOrders = new ArrayList<>();
                
                for(ExchangeOrder order : orders) {
                    BigDecimal tradedAmount = BigDecimal.ZERO;
                    BigDecimal turnover = BigDecimal.ZERO;
                    
                    ExchangeOrderDetail trade = detailsMap.getOrDefault(order.getOrderId(), null);
                    if (trade != null) {
                        tradedAmount = tradedAmount.add(trade.getAmount());
                        turnover = turnover.add(trade.getAmount().multiply(trade.getPrice()));
                    }

                    order.setTradedAmount(tradedAmount);
                    order.setTurnover(turnover);
                    
                    if(!OrderUtils.isCompleted(order)){
                        tradingOrders.add(order);
                    } else {
                        completedOrders.add(order);
                    }
                }

                if(!tradingOrders.isEmpty()) {
                    try {
                        trader.trade(tradingOrders);
                    } catch (ParseException e) {
                        log.error("Error processing trades for symbol: {}", symbol, e);
                    }
                }
                
                if(!completedOrders.isEmpty()){
                    rocketMQTemplate.convertAndSend("exchange-order-completed", JSON.toJSONString(completedOrders));
                }
                
                trader.setReady(true);
            } catch (Exception e) {
                log.error("Error initializing CoinTrader for symbol: {}", symbol, e);
                trader.setReady(true); // Set ready anyway to prevent blocking
            }
        }
    }

    private <T> List<List<T>> chunkList(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            int end = Math.min(list.size(), i + size);
            chunks.add(list.subList(i, end));
        }
        return chunks;
    }
}
