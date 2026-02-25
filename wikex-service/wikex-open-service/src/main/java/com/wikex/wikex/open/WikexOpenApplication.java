package com.wikex.wikex.open;

import com.wikex.wikex.open.interceptor.OpenApiInterceptor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.wikex.wikex.*.feign"})
@SpringBootApplication(scanBasePackages = "com.wikex.wikex")
public class WikexOpenApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(WikexOpenApplication.class,args);
    }

    @Bean
    public OpenApiInterceptor openApiInterceptor() {
        return new OpenApiInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(openApiInterceptor()).addPathPatterns("/open/**", "/user/**");
    }
}

