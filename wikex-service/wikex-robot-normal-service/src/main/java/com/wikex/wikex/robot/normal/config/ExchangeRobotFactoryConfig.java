package com.wikex.wikex.robot.normal.config;


import com.wikex.wikex.robot.normal.robot.ExchangeRobotFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExchangeRobotFactoryConfig {
	@Bean
	public ExchangeRobotFactory getFactory() {
		ExchangeRobotFactory factory = new ExchangeRobotFactory();
		return factory;
	}
}
