package com.wikex.wikex.market.consumer;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.admin.entity.DataDictionary;
import com.wikex.wikex.constant.SysConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "data-dictionary-save-update", consumerGroup = "market-data-dictionary-save-update")
public class DataDictionarySaveUpdateConsumer implements RocketMQListener<String> {
    private Logger logger = LoggerFactory.getLogger(DataDictionarySaveUpdateConsumer.class);
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void onMessage(String s) {
        try {
            DataDictionary record = JSON.parseObject(s, DataDictionary.class);
            if (record == null)
                return;

            String bond = record.getBond();
            String value = record.getValue();
            String key = SysConstant.DATA_DICTIONARY_BOUND_KEY + bond;
            ValueOperations valueOperations = redisTemplate.opsForValue();
            Object bondvalue = valueOperations.get(key);
            if (bondvalue == null) {
                valueOperations.set(key, value);
            } else {
                valueOperations.getOperations().delete(key);
                valueOperations.set(key, value);
            }
        } catch (Exception e) {
            logger.error("Critical error processing data dictionary save update message", e);
        }
    }
}
