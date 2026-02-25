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


@Component
@Slf4j
@RocketMQMessageListener(topic = "exchange-order-cancel", consumerGroup = "exchange-order-cancel-handle")
public class ExchangeOrderCancelConsumer implements RocketMQListener<String> {

    @Autowired
    private CoinTraderFactory traderFactory;
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    @Override
    public void onMessage(String s) {
        try {
            ExchangeOrder order = JSON.parseObject(s, ExchangeOrder.class);
            if(order == null){
                return ;
            }
            CoinTrader trader = traderFactory.getTrader(order.getSymbol());
            if(trader == null) {
                log.warn("Trader not found for symbol: {}", order.getSymbol());
                return;
            }
            if(trader.getReady()) {
                try {
                    ExchangeOrder result = trader.cancelOrder(order);
                    if (result != null) {
                        rocketMQTemplate.convertAndSend("exchange-order-cancel-success", JSON.toJSONString(result));
                    }
                }catch (Exception e){
                    log.error("Error canceling order: orderId={}, symbol={}", order.getOrderId(), order.getSymbol(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing or processing cancel message: {}", s, e);
        }
    }
}
