package com.wikex.wikex.swap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.service.ContractCoinService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "update-contract-coin", consumerGroup = "swap-update-contract-coin")
public class SwapUpdateCoinConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinService contractCoinService;
    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;

    @Override
    public void onMessage(String content) {
        
        if (StringUtils.isEmpty(content)) {
            return;
        }
        JSONObject json = JSON.parseObject(content);
        if (json == null) {
            return;
        }
        Long contractCoinId = json.getLong("cid");

        ContractCoin coin = contractCoinService.getById(contractCoinId);
        if(coin != null) {
            contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol()).updateContractCoin(coin);
        }

    }
}
