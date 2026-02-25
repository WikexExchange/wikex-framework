package com.wikex.wikex.coinswap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.entity.MemberContractWalletCoin;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.coinswap.service.MemberContractWalletCoinService;
import com.wikex.wikex.constant.ContractOrderPattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(topic = "member-register-coinswap", consumerGroup = "coinswap-member-register-coinswap")
public class MemberRegisterConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinCoinService contractCoinService;
    @Autowired
    private MemberContractWalletCoinService memberContractWalletService;

    @Override
    public void onMessage(String content) {
        
        if (StringUtils.isEmpty(content)) {
            return;
        }
        JSONObject json = JSON.parseObject(content);
        if (json == null) {
            return;
        }

        List<ContractCoinCoin> coins =  contractCoinService.list();
        Long memberId = json.getLong("uid");
        for(ContractCoinCoin coin:coins) {
            
            MemberContractWalletCoin wallet = memberContractWalletService.findByMemberIdAndContractCoin(memberId, coin);
            if(wallet==null){
                wallet = new MemberContractWalletCoin();
                wallet.setCoinBalance(BigDecimal.ZERO);
                wallet.setCoinBuyLeverage(BigDecimal.TEN); 
                wallet.setCoinBuyPosition(BigDecimal.ZERO);
                wallet.setCoinBuyPrice(BigDecimal.ZERO);
                wallet.setCoinBuyPrincipalAmount(BigDecimal.ZERO);
                wallet.setCoinFrozenBalance(BigDecimal.ZERO);
                wallet.setCoinFrozenBuyPosition(BigDecimal.ZERO);
                wallet.setCoinFrozenSellPosition(BigDecimal.ZERO);
                wallet.setCoinPattern(ContractOrderPattern.FIXED);
                wallet.setCoinSellLeverage(BigDecimal.TEN);
                wallet.setCoinSellPosition(BigDecimal.ZERO);
                wallet.setCoinSellPrice(BigDecimal.ZERO);
                wallet.setCoinSellPrincipalAmount(BigDecimal.ZERO);
                wallet.setCoinShareNumber(coin.getShareNumber());
                wallet.setCoinTotalProfitAndLoss(BigDecimal.ZERO);
                wallet.setContractId(coin.getId());
                wallet.setMemberId(json.getLong("uid"));
                memberContractWalletService.save(wallet);
            }

        }
    }
}
