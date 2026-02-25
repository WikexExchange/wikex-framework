package com.wikex.wikex.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.wikex.wikex.*.feign"})
@SpringBootApplication(scanBasePackages = "com.wikex.wikex")
public class WikexMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(WikexMarketApplication.class,args);
    }
}
