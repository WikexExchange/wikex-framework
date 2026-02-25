package com.wikex.wikex.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.wikex.wikex")
@EnableFeignClients(basePackages = {"com.wikex.wikex.*.feign"})
public class WikexAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(WikexAgentApplication.class,args);
    }
}
