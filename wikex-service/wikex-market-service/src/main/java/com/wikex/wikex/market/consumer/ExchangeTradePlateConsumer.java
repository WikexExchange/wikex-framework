package com.wikex.wikex.market.consumer;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.market.job.ExchangePushJob;
import com.wikex.wikex.market.processor.CoinProcessorFactory;
import com.wikex.wikex.pojo.TradePlate;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.RejectedExecutionException;

@Component
@Slf4j
@RocketMQMessageListener(topic = "exchange-trade-plate", consumerGroup = "market-exchange-trade-plate")
public class ExchangeTradePlateConsumer implements RocketMQListener<String> {
	private Logger logger = LoggerFactory.getLogger(ExchangeTradePlateConsumer.class);
	@Autowired
	private ExchangePushJob pushJob;


	@Override
	public void onMessage(String s) {
		try {
			TradePlate plate = JSON.parseObject(s, TradePlate.class);
			if (plate == null) {
				return;
			}
			String symbol = plate.getSymbol();
			pushJob.addPlates(symbol, plate);
		} catch (RejectedExecutionException e) {
			logger.error("1. market-exchange-trade-plate -> exchange-trade-plate", e);
		} catch (Exception e) {
			logger.error("2. market-exchange-trade-plate -> exchange-trade-plate", e);
		}
	}


}
