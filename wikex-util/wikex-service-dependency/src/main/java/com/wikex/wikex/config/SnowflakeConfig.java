package com.wikex.wikex.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
@Component
@Slf4j
public class SnowflakeConfig {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private long workerId ;
    private long datacenterId = 1;
    private Snowflake snowflake = IdUtil.getSnowflake(workerId,datacenterId);
    @PostConstruct
    public void init(){
        workerId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
        
    }
    public synchronized long snowflakeId(){
        return snowflake.nextId();
    }
    public synchronized long snowflakeId(long workerId,long datacenterId){
        Snowflake snowflake = IdUtil.getSnowflake(workerId, datacenterId);
        return snowflake.nextId();
    }

    public synchronized String getOrderId(String prefix){
        String body = String.valueOf(System.currentTimeMillis());
        return prefix + snowflake.nextId();
    }
}

