package com.wikex.wikex.admin.config;

import com.wikex.wikex.admin.interceptor.LogInterceptor;
import com.wikex.wikex.admin.interceptor.OutExcelInterceptor;
import com.wikex.wikex.admin.interceptor.SessionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;


@Configuration
public class ApplicationConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private FilterConfig filterConfig;

    @Bean
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/asset/**").addResourceLocations("classpath:/asset/");
        super.addResourceHandlers(registry);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(filterConfig).addPathPatterns("/**");
        registry.addInterceptor(new SessionInterceptor()).addPathPatterns("/**")
                .excludePathPatterns("/code/sms-provider/**","/announcement/more","/captcha","/system/employee/sign/in",
                        "/system/employee/check","/system/employee/logout","/system/employee/login",
                        "/noauth/exchange-coin/detail",
                        "/noauth/exchange-coin/modify-limit");
        registry.addInterceptor(new LogInterceptor()).addPathPatterns("/**");
        registry.addInterceptor(new OutExcelInterceptor()).addPathPatterns("/**/out-excel");
        super.addInterceptors(registry);
    }

}
