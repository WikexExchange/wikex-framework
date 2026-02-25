package com.wikex.wikex.swap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOrderPattern;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.MemberContractWalletService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(topic = "member-register-swap", consumerGroup = "swap-member-register-swap")
public class MemberRegisterConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinService contractCoinService;
    @Autowired
    private MemberContractWalletService memberContractWalletService;

    @Override
    public void onMessage(String content) {
        
        if (StringUtils.isEmpty(content)) {
            return;
        }
        JSONObject json = JSON.parseObject(content);
        if (json == null) {
            return;
        }
        Long memberId = json.getLong("uid");
        List<ContractCoin> coins =  contractCoinService.list();
        for(ContractCoin coin:coins) {
            MemberContractWallet wallet = memberContractWalletService.findByMemberId(memberId);
            
            if(wallet==null){
                wallet = new MemberContractWallet();
                wallet.setCoinTotalProfitAndLoss(BigDecimal.ZERO);
                wallet.setMemberId(json.getLong("uid"));
                memberContractWalletService.save(wallet);
            }
        }
    }
}
