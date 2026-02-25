package com.wikex.wikex.robot.normal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.wikex.wikex.**.feign"})
@SpringBootApplication(scanBasePackages = "com.wikex.wikex",exclude = DataSourceAutoConfiguration.class)
public class NormalApplication {
    public static void main( String[] args ){
    	SpringApplication.run(NormalApplication.class,args);
    }
}
