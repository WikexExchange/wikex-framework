package com.wikex.wikex.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.rpc.feign.RpcFeign;
import com.wikex.wikex.user.entity.Addressext;
import com.wikex.wikex.user.entity.Coinprotocol;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.feign.AddressFeign;
import com.wikex.wikex.user.feign.CoinprotocolFeign;
import com.wikex.wikex.user.feign.MemberWalletFeign;
import com.wikex.wikex.util.MessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Slf4j
@Component
@RocketMQMessageListener(topic = "reset-member-address", consumerGroup = "resetMemberAddressConsumer")
public class ResetMemberAddressConsumer implements RocketMQListener<String> {

    @Autowired
    private MemberWalletFeign memberWalletService;
    @Autowired
    private RpcFeign rpcFeign;
    @Autowired
    private AddressFeign addressFeign;

    @Autowired
    private CoinprotocolFeign coinprotocolFeign;

    @Override
    public void onMessage(String content) {
        JSONObject json = JSON.parseObject(content);
        String unit = json.getString("unit");
        Long uid = json.getLong("uid");

        MemberWallet memberWallet = memberWalletService.findByCoinUnitAndMemberId(unit, uid);
        Assert.notNull(memberWallet, "wallet null");

        Coinprotocol protocol = coinprotocolFeign.findBySymbol(unit);
        if (protocol == null) {

            return;
        }
        String account = "U" + uid;

        String serviceName = unit.toLowerCase();
        Addressext addressext = null;
        MessageResult mr = rpcFeign.getNewAddress(serviceName, account);

        if (mr != null && mr.getCode() == 0) {
            String address = mr.getData().toString();
            addressext = new Addressext();
            addressext.setMemberId(uid.longValue());
            addressext.setAddress(address);
            addressext.setCoinProtocol(protocol.getProtocol());
            addressext.setStatus(1);
            addressFeign.save(addressext);
        }
    }

}
