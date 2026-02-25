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

@Component
@Slf4j
@RocketMQMessageListener(topic = "exchange-order-cancel-${spring.application.name}", consumerGroup = "exchange-order-cancel-handle-${spring.application.name}")
public class ExchangeOrderCancelConsumer implements RocketMQListener<String> {

    @Autowired
    private CoinTraderFactory traderFactory;
    @Autowired
    private TradingConfig tradingConfig;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    @Override
    public void onMessage(String s) {
        ExchangeOrder order = JSON.parseObject(s, ExchangeOrder.class);
        if(order == null){
            return ;
        }
        CoinTrader trader = traderFactory.getTrader(order.getSymbol());
        if(trader == null){
            String serviceName =  tradingConfig.getServiceName(order.getSymbol());
            rocketMQTemplate.convertAndSend("exchange-order-cancel-"+serviceName, JSON.toJSONString(order));
            return ;
        }
        if(trader.getReady()) {
            try {
                ExchangeOrder result = trader.cancelOrder(order);
                if (result != null) {
                    rocketMQTemplate.convertAndSend("exchange-order-cancel-success", JSON.toJSONString(result));
                }
            }catch (Exception e){
                log.error("Error processing exchange order cancel, symbol={}, orderId={}", order.getSymbol(), order.getOrderId(), e);
            }
        }
    }
}
