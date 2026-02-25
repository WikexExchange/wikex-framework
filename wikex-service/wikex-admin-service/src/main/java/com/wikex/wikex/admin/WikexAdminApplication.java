package com.wikex.wikex.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.wikex.wikex")
@EnableFeignClients(basePackages = {"com.wikex.wikex.**.feign"})
@EnableCaching
@EnableDiscoveryClient
public class WikexAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(WikexAdminApplication.class,args);
    }
}
