package com.wikex.wikex.second.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.second.engine.ContractCoinMatchFactory;
import com.wikex.wikex.second.entity.ContractSecondCoin;
import com.wikex.wikex.second.handler.MongoMarketHandler;
import com.wikex.wikex.second.handler.NettyHandler;
import com.wikex.wikex.second.handler.WebsocketMarketHandler;
import com.wikex.wikex.second.service.ContractSecondCoinService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(topic = "update-contract-second-coin", consumerGroup = "update-contract-second-coin-handle")
public class UpdateContractCoinConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory; 

    @Autowired
    private ContractSecondCoinService contractSecondCoinService;

    @Autowired
    MongoMarketHandler mongoMarketHandler;

    @Autowired
    WebsocketMarketHandler wsHandler;

    @Autowired
    NettyHandler nettyHandler;

    
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

        ContractSecondCoin coin = contractSecondCoinService.getById(contractCoinId);
        if(coin != null) {
            contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol()).updateContractCoin(coin);
        }
    }

}
