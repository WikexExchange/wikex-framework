package com.wikex.wikex.coinswap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.entity.ContractOrderEntrustCoin;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "update-contract-coin-coin", consumerGroup = "coinswap-update-contract-coin-coin")
public class CoinSwapUpdateCoinConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinCoinService contractCoinService;
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

        ContractCoinCoin coin = contractCoinService.getById(contractCoinId);
        if(coin != null) {
            contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol()).updateContractCoin(coin);
        }

    }
}
