package com.wikex.wikex.market.consumer;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.NettyCommand;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.feign.ExchangeOrderFeign;
import com.wikex.wikex.market.handler.NettyHandler;
import com.wikex.wikex.market.job.ExchangePushJob;
import com.wikex.wikex.market.processor.CoinProcessor;
import com.wikex.wikex.market.processor.CoinProcessorFactory;
import com.wikex.wikex.pojo.ExchangeTrade;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RocketMQMessageListener(topic = "exchange-trade", consumerGroup = "market-exchange-trade")
public class ExchangeTradeConsumer implements RocketMQListener<String> {
	private Logger logger = LoggerFactory.getLogger(ExchangeTradeConsumer.class);
	@Autowired
	private CoinProcessorFactory coinProcessorFactory;
	@Autowired
	private SimpMessagingTemplate messagingTemplate;
	@Autowired
	private ExchangeOrderFeign exchangeOrderFeign;
	@Autowired
	private NettyHandler nettyHandler;
	@Value("${second.referrer.award}")
	private boolean secondReferrerAward;
	private ExecutorService executor = new ThreadPoolExecutor(30, 100, 60L,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(1024),
			new ThreadPoolExecutor.CallerRunsPolicy()
	);
	@Autowired
	private ExchangePushJob pushJob;

	@Override
	public void onMessage(String s) {
		try {
			List<ExchangeTrade> trades = JSON.parseArray(s,ExchangeTrade.class);
			if (trades == null || trades.isEmpty()) {
				return;
			}
			executor.submit(new HandleTradeThread(trades));
		} catch (RejectedExecutionException e) {
			logger.error("Thread pool is full, cannot process trade message. Consider increasing pool size or queue capacity.", e);
		} catch (Exception e) {
			logger.error("Error parsing or submitting trade message", e);
		}
	}


	public class HandleTradeThread implements Runnable {
		private List<ExchangeTrade> trades;

		private HandleTradeThread(List<ExchangeTrade> trades) {
			this.trades = trades;
		}

		@Override
		public void run() {
			try {
				if (trades == null || trades.isEmpty()) {
					return;
				}
				String symbol = trades.get(0).getSymbol();
				CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol);
				Set<String> orderIds = new HashSet<>();
				for (ExchangeTrade trade : trades) {
					if (trade.getBuyOrderId() != null) {
						orderIds.add(trade.getBuyOrderId());
					}
					if (trade.getSellOrderId() != null) {
						orderIds.add(trade.getSellOrderId());
					}
				}
				Map<String, ExchangeOrder> orderMap = new HashMap<>();
				if (!orderIds.isEmpty()) {
					try {
						List<ExchangeOrder> orders = exchangeOrderFeign.findByOrderIds(new ArrayList<>(orderIds));
						if (orders != null) {
							for (ExchangeOrder order : orders) {
								if (order != null && order.getOrderId() != null) {
									orderMap.put(order.getOrderId(), order);
								}
							}
						}
					} catch (Exception e) {
						logger.error("Failed to fetch orders by ids for symbol: {}", symbol, e);
					}
				}

				for (ExchangeTrade trade : trades) {
					try {
						exchangeOrderFeign.processExchangeTrade(trade, secondReferrerAward);
					} catch (Exception e) {
						logger.error("Failed to process exchange trade for symbol: {}", symbol, e);
					}
					
					ExchangeOrder buyOrder = orderMap.get(trade.getBuyOrderId());
					ExchangeOrder sellOrder = orderMap.get(trade.getSellOrderId());
					
					if (buyOrder != null) {
						try {
							messagingTemplate.convertAndSend("/topic/market/order-trade/" + symbol + "/" + buyOrder.getMemberId(), buyOrder);
							nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_TRADE, buyOrder);
						} catch (Exception e) {
							logger.error("Failed to push buy order notification for orderId: {}", buyOrder.getOrderId(), e);
						}
					}
					if (sellOrder != null) {
						try {
							messagingTemplate.convertAndSend("/topic/market/order-trade/" + symbol + "/" + sellOrder.getMemberId(), sellOrder);
							nettyHandler.handleOrder(NettyCommand.PUSH_EXCHANGE_ORDER_TRADE, sellOrder);
						} catch (Exception e) {
							logger.error("Failed to push sell order notification for orderId: {}", sellOrder.getOrderId(), e);
						}
					}
				}
				
				if (coinProcessor != null) {
					try {
						coinProcessor.process(trades);
					} catch (Exception e) {
						logger.error("Failed to process trades in coin processor for symbol: {}", symbol, e);
					}
				}
				
				try {
					pushJob.addTrades(symbol, trades);
				} catch (Exception e) {
					logger.error("Failed to add trades to push job for symbol: {}", symbol, e);
				}
			} catch (Exception e) {
				logger.error("Critical error processing trades batch", e);
			}
		}
	}
}
