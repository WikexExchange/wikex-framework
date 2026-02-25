package com.wikex.wikex.market.consumer;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.NettyCommand;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.feign.ExchangeOrderFeign;
import com.wikex.wikex.market.handler.NettyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@RocketMQMessageListener(topic = "exchange-order-cancel-success", consumerGroup = "market-exchange-order-cancel-success")
public class ExchangeTradeCancelSuccessConsumer implements RocketMQListener<String> {
	private Logger logger = LoggerFactory.getLogger(ExchangeTradeCancelSuccessConsumer.class);

	@Autowired
	private ExchangeOrderFeign exchangeOrderFeign;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private NettyHandler nettyHandler;


	@Override
	public void onMessage(String s) {
		try {
			ExchangeOrder order = JSON.parseObject(s, ExchangeOrder.class);
			String symbol = order.getSymbol();

			try {
				exchangeOrderFeign.cancelOrder(order.getOrderId(), order.getTradedAmount(), order.getTurnover() == null ? BigDecimal.ZERO : order.getTurnover());
			} catch (Exception e) {
				logger.error("Failed to mark trade as cancel for orderId: {}, symbol: {}", order.getOrderId(), symbol, e);
			}

			try {
				messagingTemplate.convertAndSend("/topic/market/order-canceled/" + symbol + "/" + order.getMemberId(), order);
				nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_CANCELED, order);
			} catch (Exception e) {
				logger.error("Failed to push order cancel notification for orderId: {}, symbol: {}", order.getOrderId(), symbol, e);
			}
		} catch (Exception e) {
			logger.error("Critical error processing order completed message", e);
		}
	}


}
