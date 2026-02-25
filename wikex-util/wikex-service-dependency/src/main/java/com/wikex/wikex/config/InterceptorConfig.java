package com.wikex.wikex.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterceptorConfig {
    private Logger logger = LoggerFactory.getLogger(InterceptorConfig.class);
    @Bean
    public RequestInterceptor cloudContextInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String url = template.url();
                if (url.contains("$chain")) {
                    String resolvedServiceName = route(template);
                    url = url.replace("/"+resolvedServiceName, "");
                    url = url.replace("$chain", resolvedServiceName);
                    template.uri(url);
                }
                if (url.startsWith("//")) {
                    url = "http:" + url.split("\\?")[0];
                    template.target(url);
                    template.uri("");
                }
            }

            private String route(RequestTemplate template) {
                String serviceName = null;
                String url = template.url();
                if (url.contains("/rpc/")) {
                    String[] urlParts = url.split("/");
                    for (int i = 0; i < urlParts.length; i++) {
                        if (urlParts[i].equals("rpc")) {
                            if (i < urlParts.length - 1) {
                                serviceName = urlParts[i + 1];
                            }
                            break;
                        }
                    }
                }
                if (serviceName == null) {
                    serviceName = "";
                }
                return serviceName;
            }
        };
    }

    public static void main(String[] args) {
        String url = "//wikex-rpc-trx/rpc/setPassword?password=wikex";
        System.out.println(url.split("\\?")[0]);
    }
}
