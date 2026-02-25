package com.wikex.wikex.robot.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.wikex.wikex.**.feign"})
@SpringBootApplication(scanBasePackages = "com.wikex.wikex",exclude = DataSourceAutoConfiguration.class)
public class MarketApplication {
    public static void main( String[] args ){
    	SpringApplication.run(MarketApplication.class,args);
    }
}
