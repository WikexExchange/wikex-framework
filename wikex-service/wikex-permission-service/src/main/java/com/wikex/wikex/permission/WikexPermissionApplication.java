package com.wikex.wikex.permission;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.wikex.wikex")
@MapperScan(basePackages = "com.wikex.wikex.permission.mapper")
public class WikexPermissionApplication {

    public static void main(String[] args) {
        SpringApplication.run(WikexPermissionApplication.class,args);
    }
}
