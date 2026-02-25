package com.wikex.wikex.robot.market.event;

import com.wikex.wikex.robot.market.engine.MarketEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEvent implements ApplicationRunner {

	private final static Logger logger = LoggerFactory.getLogger(ApplicationEvent.class);

	@Autowired
	private MarketEngineFactory marketEngineFactory;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		logger.info("===============================================");
		logger.info("=========== Market data sync started ==========");
		logger.info("===============================================");
		logger.info("========= Okex === Huobi === Zb === Biki === FX ========");
		logger.info("===============================================");
		logger.info("============ https://ui.tooldex.io ============");
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	}

}
