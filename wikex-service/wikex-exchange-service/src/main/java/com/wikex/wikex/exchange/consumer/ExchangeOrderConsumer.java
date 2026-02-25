package com.wikex.wikex.exchange.consumer;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.exchange.Trader.CoinTrader;
import com.wikex.wikex.exchange.Trader.CoinTraderFactory;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "exchange-order", consumerGroup = "exchange-order-handle")
public class ExchangeOrderConsumer implements RocketMQListener<String> {

    @Autowired
    private CoinTraderFactory traderFactory;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    @Override
    public void onMessage(String s) {
        try {
            ExchangeOrder order = JSON.parseObject(s, ExchangeOrder.class);
            if(order == null || order.getOrderId()==null){
                return ;
            }
            CoinTrader trader = traderFactory.getTrader(order.getSymbol());
            
            if (trader == null) {
                log.warn("Trader not found for symbol: {}", order.getSymbol());
                return;
            }
            
            if (trader.isTradingHalt() || !trader.getReady()) {
                rocketMQTemplate.convertAndSend("exchange-order-cancel-success", JSON.toJSONString(order));
            } else {
                try {
                    trader.trade(order);
                } catch (Exception e) {
                    rocketMQTemplate.convertAndSend("exchange-order-cancel-success", JSON.toJSONString(order));
                    log.error("Error processing trade for orderId: {}, symbol: {}", order.getOrderId(), order.getSymbol(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing or processing message: {}", s, e);
        }
    }
}
