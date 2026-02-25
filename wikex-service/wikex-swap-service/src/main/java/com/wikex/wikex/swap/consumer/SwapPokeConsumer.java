package com.wikex.wikex.swap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.pojo.ContractTrade;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.job.ExchangePushJob;
import com.wikex.wikex.swap.service.ContractCoinService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(topic = "admin-save-swap-poke", consumerGroup = "swap-admin-save-swap-poke")
public class SwapPokeConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinService contractCoinService;
    @Autowired
    private ContractCoinMatchFactory matchFactory;

    @Autowired
    private ExchangePushJob exchangePushJob;

    @Override
    public void onMessage(String content) {
        
        if (StringUtils.isEmpty(content)) {
            return;
        }
        JSONObject json = JSON.parseObject(content);
        if (json == null) {
            return;
        }
        contractCoinService.savePoke(json.getString("symbol"),json.getBigDecimal("price"));
        String symbol = json.getString("symbol");
        BigDecimal price = json.getBigDecimal("price");

        BigDecimal amount = BigDecimal.valueOf(Math.random());
        long time  = new Date().getTime();
        
        ContractTrade trade = new ContractTrade();
        trade.setAmount(amount);
        trade.setPrice(price);
        if(time % 2 == 0 ) {
            trade.setDirection(ContractOrderDirection.BUY);
            trade.setBuyOrderId("P"+time);
            trade.setBuyTurnover(amount.multiply(price));
        }else{
            trade.setDirection(ContractOrderDirection.SELL);
            trade.setSellOrderId("P"+time+"1");
            trade.setSellTurnover(amount.multiply(price));
        }
        trade.setSymbol(symbol);
        trade.setTime(time);
        
        List<ContractTrade> ts = new ArrayList<>();
        ts.add(trade);
        exchangePushJob.pushTickTrade(symbol,ts);


    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(Math.random());
        }

    }
}
