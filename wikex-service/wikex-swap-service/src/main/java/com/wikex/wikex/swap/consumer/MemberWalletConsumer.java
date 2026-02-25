package com.wikex.wikex.swap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.service.ContractCoinService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(topic = "member-wallet-change", consumerGroup = "swap-member-wallet-change")
public class MemberWalletConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory; 
    @Autowired
    private ContractCoinService contractCoinService;
    @Override
    public void onMessage(String content) {
        
        if (StringUtils.isEmpty(content)) {
            return;
        }
        JSONObject json = JSON.parseObject(content);
        if (json == null) {
            return;
        }
        
        List<ContractCoin> coins =  contractCoinService.list();
        for (ContractCoin coin : coins) {
            ContractCoinMatch contractCoinMatch = contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol());
            if(contractCoinMatch!=null){
                contractCoinMatch.memberWalletChange(json.getLong("walletId"));
            }
        }


    }
}
