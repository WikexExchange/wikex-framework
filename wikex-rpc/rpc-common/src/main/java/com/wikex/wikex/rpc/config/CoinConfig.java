package com.wikex.wikex.rpc.config;

import com.wikex.wikex.rpc.entity.Coin;
import com.wikex.wikex.rpc.entity.WatcherSetting;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Automatically configure coin parameters
 */
@Configuration
@ConditionalOnProperty(name = "coin.name")
public class CoinConfig {

    @Bean
    @ConfigurationProperties(prefix = "coin")
    public Coin getCoin() {
        Coin coin = new Coin();
        return coin;
    }

    @Bean
    @ConfigurationProperties(prefix = "watcher")
    public WatcherSetting getWatcherSetting() {
        WatcherSetting setting = new WatcherSetting();
        return setting;
    }

}
