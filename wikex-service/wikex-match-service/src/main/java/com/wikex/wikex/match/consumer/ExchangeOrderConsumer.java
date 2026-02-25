package com.wikex.wikex.match.consumer;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.match.config.TradingConfig;
import com.wikex.wikex.match.trader.CoinTrader;
import com.wikex.wikex.match.trader.CoinTraderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "exchange-order-${spring.application.name}", consumerGroup = "exchange-order-handle-${spring.application.name}")
public class ExchangeOrderConsumer implements RocketMQListener<String> {

    @Autowired
    private CoinTraderFactory traderFactory;
    @Autowired
    private TradingConfig tradingConfig;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    @Override
    public void onMessage(String s) {

        ExchangeOrder order = JSON.parseObject(s, ExchangeOrder.class);
        if(order == null || order.getOrderId()==null){
            return ;
        }
        CoinTrader trader = traderFactory.getTrader(order.getSymbol());
        if(trader == null){
            String serviceName =  tradingConfig.getServiceName(order.getSymbol());
            rocketMQTemplate.convertAndSend("exchange-order-"+serviceName, JSON.toJSONString(order));
            return ;
        }
        
        if (trader.isTradingHalt() || !trader.getReady()) {
            
            rocketMQTemplate.convertAndSend("exchange-order-cancel-success", JSON.toJSONString(order));
        } else {
            try {
                trader.trade(order);
            } catch (Exception e) {
                rocketMQTemplate.convertAndSend("exchange-order-cancel-success", JSON.toJSONString(order));
                log.error("Error processing exchange order, symbol={}, orderId={}", order.getSymbol(), order.getOrderId(), e);
            }
        }
    }
}
