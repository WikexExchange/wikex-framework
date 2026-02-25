package com.wikex.wikex.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.wikex.wikex", exclude = { DataSourceAutoConfiguration.class })
@EnableFeignClients(basePackages = { "com.wikex.wikex.blog.feign" })
@EnableMongoRepositories(basePackages = "com.wikex.wikex.blog.repository")
public class WikexBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(WikexBlogApplication.class, args);
    }
}
