package com.wikex.wikex.swap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.handler.MongoMarketHandler;
import com.wikex.wikex.swap.handler.NettyHandler;
import com.wikex.wikex.swap.handler.WebsocketMarketHandler;
import com.wikex.wikex.swap.job.ExchangePushJob;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.ContractOrderEntrustService;
import com.wikex.wikex.swap.service.MemberContractWalletService;
import com.wikex.wikex.swap.service.MemberTradeLimitService;
import com.wikex.wikex.swap.util.WikexWebSocketConnectionManage;
import com.wikex.wikex.swap.util.HuobiWebSocketConnectionManage;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RocketMQMessageListener(topic = "add-contract-coin", consumerGroup = "swap-add-contract-coin")
public class SwapAddCoinConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;
    @Autowired
    private ContractCoinService contractCoinService;
    @Autowired
    private ContractOrderEntrustService contractOrderEntrusdtService;
    @Autowired
    private MemberTransactionFeign memberTransactionFeign;
    @Autowired
    private MemberContractWalletService memberContractWalletService;
    @Autowired
    private MemberTradeLimitService memberTradeLimitService;
    @Autowired
    private MongoMarketHandler mongoMarketHandler;
    @Autowired
    private WebsocketMarketHandler wsHandler;
    @Autowired
    private NettyHandler nettyHandler;
    @Autowired
    private ExchangePushJob exchangePushJob;
    @Value("${platformCoins}")
    private String platformCoins;

    @Override
    public void onMessage(String content) {
        
        if (StringUtils.isEmpty(content)) {
            return;
        }
        JSONObject json = JSON.parseObject(content);
        if (json == null) {
            return;
        }
        Long contractCoinId = json.getLong("id");

        ContractCoin coin = contractCoinService.getById(contractCoinId);
        if(coin != null) {
            
            ContractCoinMatch match = new ContractCoinMatch(coin.getSymbol());
            match.setContractCoinService(contractCoinService);
            match.setContractOrderEntrustService(contractOrderEntrusdtService);
            match.setMemberTransactionFeign(memberTransactionFeign);
            match.setMemberContractWalletService(memberContractWalletService);
            match.setMemberTradeLimitService(memberTradeLimitService);
            match.addHandler(mongoMarketHandler);
            match.addHandler(wsHandler);
            match.addHandler(nettyHandler);
            match.setExchangePushJob(exchangePushJob);
            match.run();
            contractCoinMatchFactory.addContractCoinMatch(coin.getSymbol(), match);

            
            List<String> platformCoinList =  Arrays.stream(platformCoins.split(",")).collect(Collectors.toList());

            if(platformCoinList.contains(coin.getSymbol().split("/")[0]) || platformCoinList.contains(coin.getSymbol().split("/")[1])){
                
                WikexWebSocketConnectionManage.getWebSocket().subNewCoin(coin.getSymbol());
            }else {
                
                HuobiWebSocketConnectionManage.getWebSocket().subNewCoin(coin.getSymbol());
            }
        }

    }
}
