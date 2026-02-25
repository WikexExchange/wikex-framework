package com.wikex.wikex.coinswap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.entity.ContractOrderEntrustCoin;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Slf4j
@Component
@RocketMQMessageListener(topic = "swap-coin-order-open", consumerGroup = "coinswap-swap-coin-order-open")
public class CoinSwapOrderOpenConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;

    @Override
    public void onMessage(String content) {
        
        if (StringUtils.isEmpty(content)) {
            return;
        }
        ContractOrderEntrustCoin order = JSON.parseObject(content, ContractOrderEntrustCoin.class);
        if (order == null) {
            
            return;
        }
        
        try {
            contractCoinMatchFactory.getContractCoinMatch(order.getSymbol()).trade(order);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
