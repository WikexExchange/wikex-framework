package com.wikex.wikex.user.mq;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.rpc.feign.RpcFeign;
import com.wikex.wikex.user.service.CoinService;
import com.wikex.wikex.user.service.MemberWalletService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(topic = "reset-member-address", consumerGroup = "user-member-reset-address")
public class MemberResetAddressConsumer implements RocketMQListener<String> {

    @Autowired
    private CoinService coinService;
    @Autowired
    private RpcFeign rpcFeign;
    @Autowired
    private MemberWalletService memberWalletService;

    @Override
    public void onMessage(String content) {

        if (StringUtils.isEmpty(content)) {
            return;
        }
        JSONObject json = JSON.parseObject(content);
        if (json == null) {
            return;
        }

    }

}
