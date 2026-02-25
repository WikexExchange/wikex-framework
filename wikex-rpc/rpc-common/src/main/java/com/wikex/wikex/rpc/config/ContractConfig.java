package com.wikex.wikex.rpc.config;

import com.wikex.wikex.rpc.entity.Contract;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Automatically configure contract parameters
 */
@Configuration
@ConditionalOnProperty(name = "contract.address")
public class ContractConfig {

    @Bean
    @ConfigurationProperties(prefix = "contract")
    public Contract getContract() {
        return new Contract();
    }

}
