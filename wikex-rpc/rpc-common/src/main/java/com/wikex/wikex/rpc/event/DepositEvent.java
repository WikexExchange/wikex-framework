package com.wikex.wikex.rpc.event;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.rpc.entity.Deposit;
import com.wikex.wikex.rpc.service.DepositService;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

@Component
public class DepositEvent {
    private Logger logger = LoggerFactory.getLogger(DepositEvent.class);
    @Autowired
    private DepositService depositService;
    @Autowired
    private RocketMQTemplate rocketMQTemplate;


    public synchronized void onConfirmed(Deposit deposit){
        if(!depositService.exists(deposit)) {
            logger.info("confirmed deposit,tx={} address={} amount={}",deposit.getTxid(),deposit.getAddress(),deposit.getAmount());
            depositService.save(deposit);
            rocketMQTemplate.convertAndSend("deposit", JSON.toJSONString(deposit));
        }
    }
}
