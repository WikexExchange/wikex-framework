package com.wikex.wikex.user.config;

import com.wikex.wikex.user.system.GeetestLib;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GeetestConfig {
    @Value("${geetest.captchaId}")
    private String captchaId;
    @Value("${geetest.privateKey}")
    private String privateKey;
    @Value("${geetest.newFailback}")
    private int newFailback;

    @Bean
    public GeetestLib geetestLib() {
        return new GeetestLib(captchaId, privateKey, newFailback == 0 ? false : true);
    }
}
