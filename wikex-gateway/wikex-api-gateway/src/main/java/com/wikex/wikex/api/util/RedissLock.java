package com.wikex.wikex.api.util;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissLock {

    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private String redisPort;
    @Value("${spring.redis.password}")
    private String redisPwd;

    /***
     * Create RedissonClient object
     *      Create lock and unlock
     * @return
     */
    @Bean
    public RedissonClient redissonClient(){
        // Create Config
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+redisHost+":"+redisPort).setPassword(redisPwd);
        //Cluster implementation
//        config.useClusterServers()
//                .setScanInterval(2000)
//                .addNodeAddress(
//                        "redis://192.168.100.130:7001",
//                        "redis://192.168.100.130:7002",
//                        "redis://192.168.100.130:7003",
//                        "redis://192.168.100.130:7004",
//                        "redis://192.168.100.130:7005",
//                        "redis://47.242.254.112:6379");
        // Create RedissonClient
        return Redisson.create(config);
    }
}
