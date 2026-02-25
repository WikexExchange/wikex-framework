package com.wikex.wikex.robot.normal.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationEvent implements ApplicationRunner {
	
	private final static  Logger logger  =  LoggerFactory.getLogger(ApplicationEvent.class);

	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		logger.info("===============================================");
		logger.info("===============Trading robot initialization===============");
		logger.info("===============================================");
	}

}
