package com.wikex.wikex.api.hot;

import com.alibaba.fastjson.JSON;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/***
 * Queue operations
 */
@Component
public class HotQueue {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    // Product is not hot
    public static final Integer NOT_HOT=0;
    // Already in queue
    public static final Integer HAS_QUEUE=204;
    // Queueing successful
    public static final Integer QUEUE_ING=200;


    /***
     * Queue for flash sale orders
     * @param username the username
     * @param id product ID
     * @param num quantity
     * @return status code
     */
    public int hotToQueue(String username,String id,Integer num){
        // Get product info from Redis; if exists, product is hot
        Boolean bo = redisTemplate.boundHashOps("HotSeckillGoods").hasKey(id);
        if(!bo){
            // Product is not hot
            return NOT_HOT;
        }
        // Avoid duplicate queueing
        Long increment = redisTemplate.boundValueOps("OrderQueue" + username).increment(1);
        if(increment>1){
            // Please do not queue again
            return HAS_QUEUE;
        }
        // Expiration time
        redisTemplate.boundValueOps("OrderQueue" + username).expire(2, TimeUnit.MINUTES);

        // Perform queue operation
        Map<String,Object> dataMap = new HashMap<String,Object>();
        dataMap.put("username",username);
        dataMap.put("id",id);
        dataMap.put("num",num);
        Message<String> message = MessageBuilder.withPayload(JSON.toJSONString(dataMap)).build();
        rocketMQTemplate.convertAndSend("order-queue",message);
        return QUEUE_ING;
    }
}
