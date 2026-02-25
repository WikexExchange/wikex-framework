package com.wikex.wikex.config;

import com.uduncloud.sdk.client.UdunClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UdunClientConfig {

    @Value("${udun.merchantId}")
    private String merchantId;
    @Value("${udun.merchantKey}")
    private String merchantKey;
    @Value("${udun.gateway}")
    private String gateway;
    @Value("${server.host}")
    private String host;

    @Bean
    public UdunClient setUdunClient(){
        UdunClient udunClient = new UdunClient(gateway,
                merchantId,
                merchantKey,
                host+"/wallet/notify");
        return udunClient;
    }
}
