package com.wikex.wikex.kline;


import com.wikex.wikex.kline.service.impl.BZKlineRobotMarketService;

import com.wikex.wikex.kline.service.impl.JLKlineRobotMarketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
//@EnableFeignClients(basePackages = {"com.wikex.wikex.*.feign"})
@SpringBootApplication(scanBasePackages = "com.wikex.wikex")
public class KlineToolsApplication {

    public static void main(String[] args){
        SpringApplication.run(KlineToolsApplication.class,args);
    }

    @ConditionalOnProperty(name ="k.for",havingValue = "bz")
    @Bean
    public BZKlineRobotMarketService getBZKlineRobotMarketService(){
        return new BZKlineRobotMarketService();
    }

    @ConditionalOnProperty(name ="k.for",havingValue = "jl")
    @Bean
    public JLKlineRobotMarketService getJLKlineRobotMarketService(){
        return new JLKlineRobotMarketService();
    }
}
