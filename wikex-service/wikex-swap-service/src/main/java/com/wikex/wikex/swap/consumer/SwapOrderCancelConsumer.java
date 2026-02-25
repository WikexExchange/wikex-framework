package com.wikex.wikex.swap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractOrderEntrust;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "swap-order-cancel", consumerGroup = "swap-swap-order-cancel")
public class SwapOrderCancelConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;

    @Override
    public void onMessage(String content) {
        
        if (StringUtils.isEmpty(content)) {
            return;
        }
        ContractOrderEntrust order = JSON.parseObject(content, ContractOrderEntrust.class);
        if (order == null) {
            
            return;
        }
        
        contractCoinMatchFactory.getContractCoinMatch(order.getSymbol()).cancelContractOrderEntrust(order, false);

    }
}
