// package com.wikex.wikex.user.mq;

// import com.alibaba.druid.util.StringUtils;
// import com.alibaba.fastjson.JSON;
// import com.alibaba.fastjson.JSONObject;
// import com.wikex.wikex.user.entity.Addressext;
// import com.wikex.wikex.user.entity.Coin;
// import com.wikex.wikex.user.entity.Coinext;
// import com.wikex.wikex.user.feign.AddressFeign;
// import com.wikex.wikex.user.feign.CoinFeign;
// import com.wikex.wikex.user.feign.CoinextFeign;
// import com.wikex.wikex.user.feign.MemberWalletFeign;
// import com.wikex.wikex.user.service.*;
// import com.wikex.wikex.util.MessageResult;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
// import org.apache.rocketmq.spring.core.RocketMQListener;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;

// import java.math.BigDecimal;

// @Slf4j
// @Component
// @RocketMQMessageListener(topic = "deposit", consumerGroup =
// "financeConsumer-deposit")
// public class FinanceConsumer implements RocketMQListener<String> {
// @Autowired
// private CoinService coinService;
// @Autowired
// private MemberFundingWalletService memberFundingWalletService;
// @Autowired
// private MemberDepositService memberDepositService;
// @Autowired
// private CoinextService coinextService;
// @Autowired
// private AddressextService addressextService;

// @Override
// public void onMessage(String content) {

// if (StringUtils.isEmpty(content)) {
// return;
// }
// JSONObject json = JSON.parseObject(content);
// if (json == null) {
// return;
// }

// BigDecimal amount = json.getBigDecimal("amount");
// String txid = json.getString("txid");
// String address = json.getString("address");
// String fromAddress = json.getString("fromAddress");
// String protocol = json.getString("protocol");
// Long blockHeight = json.getLong("blockHeight");
// String coinUnit = json.getString("coinName");
// Coin coin = coinService.findByUnit(coinUnit);
// Addressext addressext = addressextService.findByAddress(address);
// if (addressext == null) {
// return;
// }
// Coinext coinext =
// coinextService.findFirstByCoinNameAndProtocol(coin.getName(),
// addressext.getCoinProtocol());
// if (coinext == null) {
// return;
// }
// if (amount.compareTo(coinext.getMinRecharge()) >= 0) {

// if (coin != null && memberDepositService.findDeposit(address, txid) == null)
// {
// MessageResult mr = memberFundingWalletService.recharge(coin.getUnit(),
// address, amount, txid,
// fromAddress, blockHeight);
// }
// } else {

// }
// }

// }
