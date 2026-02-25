//package com.wikex.wikex.open.config;
//
//import com.wikex.wikex.open.interceptor.OpenApiInterceptor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
//

////@Configuration
//public class ApplicationConfig extends WebMvcConfigurerAdapter {
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new OpenApiInterceptor())
//                .addPathPatterns("/open/**", "/user/**")
//                .excludePathPatterns("/open/**");
//        super.addInterceptors(registry);
//    }
//
//
//}
