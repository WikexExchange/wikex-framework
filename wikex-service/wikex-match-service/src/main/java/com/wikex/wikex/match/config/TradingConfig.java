package com.wikex.wikex.match.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Configuration
@ConfigurationProperties(prefix = "trading")
public class TradingConfig {

    private String serviceName;
    private Map<String,List<String>> pairs;

    public String getServiceName(String symbol) {
        Map<String, List<String>> pairs = getPairs();
        String serviceName = null;
        if(pairs == null){
            return getServiceName();
        }
        Set<String> keySet = pairs.keySet();
        for (String key : keySet) {
            List<String> list = pairs.get(key);
            if (list.contains(symbol)) {
                serviceName = key;
                break;
            }
        }
        if(serviceName == null){
            serviceName =  getServiceName();
        }

        return serviceName;

    }
}
