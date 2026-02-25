package com.wikex.wikex.coinswap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.coinswap.engine.ContractCoinMatch;
import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.handler.MongoMarketHandler;
import com.wikex.wikex.coinswap.handler.NettyHandler;
import com.wikex.wikex.coinswap.handler.WebsocketMarketHandler;
import com.wikex.wikex.coinswap.job.ExchangePushJob;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.coinswap.service.ContractOrderEntrustCoinService;
import com.wikex.wikex.coinswap.service.MemberContractWalletCoinService;
import com.wikex.wikex.coinswap.util.WikexWebSocketConnectionManage;
import com.wikex.wikex.coinswap.util.HuobiWebSocketConnectionManage;
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
@RocketMQMessageListener(topic = "add-contract-coin-coin", consumerGroup = "coinswap-add-contract-coin-coin")
public class CoinSwapAddCoinConsumer implements RocketMQListener<String> {

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;
    @Autowired
    private ContractCoinCoinService contractCoinService;
    @Autowired
    private ContractOrderEntrustCoinService contractOrderEntrusdtService;
    @Autowired
    private MemberTransactionFeign memberTransactionFeign;
    @Autowired
    private MemberContractWalletCoinService memberContractWalletCoinService;
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

        ContractCoinCoin coin = contractCoinService.getById(contractCoinId);
        if(coin != null) {
            
            ContractCoinMatch match = new ContractCoinMatch(coin.getSymbol());
            match.setContractCoinService(contractCoinService);
            match.setContractOrderEntrustService(contractOrderEntrusdtService);
            match.setMemberTransactionFeign(memberTransactionFeign);
            match.setMemberContractWalletService(memberContractWalletCoinService);
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
