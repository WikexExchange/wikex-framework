package com.wikex.wikex.permission.init;

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



    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+redisHost+":"+redisPort).setPassword(redisPwd);
//        config.useClusterServers()
//                .setScanInterval(2000)
//                .addNodeAddress(
//                        "redis://47.242.254.112:6379",
//                        "redis://192.168.100.130:7002",
//                        "redis://192.168.100.130:7003",
//                        "redis://192.168.100.130:7004",
//                        "redis://192.168.100.130:7005",
//                        "redis://47.242.254.112:6379");
        return Redisson.create(config);
    }
}
