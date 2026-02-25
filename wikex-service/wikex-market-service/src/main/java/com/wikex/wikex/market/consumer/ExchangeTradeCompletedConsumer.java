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

import java.util.List;

@Component
@Slf4j
@RocketMQMessageListener(topic = "exchange-order-completed", consumerGroup = "market-exchange-order-completed")
public class ExchangeTradeCompletedConsumer implements RocketMQListener<String> {
	private Logger logger = LoggerFactory.getLogger(ExchangeTradeCompletedConsumer.class);
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private ExchangeOrderFeign exchangeOrderFeign;
	@Autowired
	private NettyHandler nettyHandler;


	@Override
	public void onMessage(String s) {
		try {
			List<ExchangeOrder> orders = JSON.parseArray(s, ExchangeOrder.class);
			if (orders == null || orders.isEmpty()) {
				return;
			}
			for (ExchangeOrder order : orders) {
				try {
					String symbol = order.getSymbol();
					
					try {
						exchangeOrderFeign.tradeCompleted(order.getOrderId(), order.getTradedAmount(), order.getTurnover());
					} catch (Exception e) {
						logger.error("Failed to mark trade as completed for orderId: {}, symbol: {}", order.getOrderId(), symbol, e);
					}
					
					try {
						messagingTemplate.convertAndSend("/topic/market/order-completed/" + symbol + "/" + order.getMemberId(), order);
						nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_COMPLETED, order);
					} catch (Exception e) {
						logger.error("Failed to push order completed notification for orderId: {}, symbol: {}", order.getOrderId(), symbol, e);
					}
				} catch (Exception e) {
					logger.error("Error processing order: {}", order.getOrderId(), e);
				}
			}
		} catch (Exception e) {
			logger.error("Critical error processing order completed message", e);
		}
	}


}
