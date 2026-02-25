package com.wikex.wikex;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.wikex.wikex.*.feign"})
@SpringBootApplication(scanBasePackages = "com.wikex.wikex")
public class WalletApplication {
    public static void main(String[] args){
        SpringApplication.run(WalletApplication.class,args);
    }
}
