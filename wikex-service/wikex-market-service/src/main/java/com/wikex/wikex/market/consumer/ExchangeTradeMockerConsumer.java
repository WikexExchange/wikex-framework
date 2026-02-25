package com.wikex.wikex.market.consumer;

import com.alibaba.fastjson.JSON;
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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

@Component
@Slf4j
@RocketMQMessageListener(topic = "exchange-trade-mocker", consumerGroup = "market-exchange-trade-mocker")
public class ExchangeTradeMockerConsumer implements RocketMQListener<String> {
	private Logger logger = LoggerFactory.getLogger(ExchangeTradeMockerConsumer.class);
	@Autowired
	private ExchangePushJob pushJob;
	@Autowired
	private CoinProcessorFactory coinProcessorFactory;


	@Override
	public void onMessage(String s) {
		try {
			List<ExchangeTrade> trades = JSON.parseArray(s, ExchangeTrade.class);
			if (trades == null || trades.isEmpty()) {
				return;
			}

			String symbol = trades.get(0).getSymbol();
			CoinProcessor coinProcessor = coinProcessorFactory.getProcessor(symbol);
			if (coinProcessor != null) {
				coinProcessor.process(trades);
			}
			pushJob.addTrades(symbol, trades);
		} catch (RejectedExecutionException e) {
			logger.error("1. market-exchange-trade-mocker -> exchange-trade-mocker", e);
		} catch (Exception e) {
			logger.error("2. market-exchange-trade-mocker -> exchange-trade-mocker", e);
		}
	}


}
