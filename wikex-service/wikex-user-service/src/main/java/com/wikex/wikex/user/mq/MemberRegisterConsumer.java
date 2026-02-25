package com.wikex.wikex.user.mq;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.SettingNameConstant;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.entity.Settings;
import com.wikex.wikex.user.service.CoinService;
import com.wikex.wikex.user.service.MemberWalletService;
import com.wikex.wikex.user.service.SettingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(topic = "member-register", consumerGroup = "user-member-register")
public class MemberRegisterConsumer implements RocketMQListener<String> {

    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberWalletService memberWalletService;
    @Autowired
    private SettingService settingService;

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

        Integer usdt = 0;
        Settings setting = settingService.findByName(SettingNameConstant.REGISTER_SUCCESS_EARN_BALANCE_USDT);
        if (setting != null) {
            usdt = Integer.valueOf(setting.getValue().trim());
        }

        List<Coin> coins = coinService.list();
        for (Coin coin : coins) {

            MemberWallet wallet = memberWalletService.findByCoinUnitAndMemberId(coin.getUnit(), memberId);
            if (wallet == null) {
                wallet = new MemberWallet();
                wallet.setCoinId(coin.getUnit());
                wallet.setMemberId(memberId);
                if (coin.getUnit().toLowerCase().equals("usdt"))
                    wallet.setBalance(new BigDecimal(usdt));
                else
                    wallet.setBalance(new BigDecimal(0));
                wallet.setFrozenBalance(new BigDecimal(0));

                memberWalletService.save(wallet);
            }

        }

    }
}
