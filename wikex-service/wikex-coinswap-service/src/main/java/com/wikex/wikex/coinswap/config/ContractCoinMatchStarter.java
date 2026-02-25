package com.wikex.wikex.coinswap.config;

import com.wikex.wikex.coinswap.socket.client.WsClientwikex;
import com.wikex.wikex.coinswap.util.WikexWebSocketConnectionManage;
import com.wikex.wikex.coinswap.util.HuobiWebSocketConnectionManage;
import com.wikex.wikex.config.SnowflakeConfig;
import com.wikex.wikex.coinswap.client.Client;
import com.wikex.wikex.coinswap.engine.ContractCoinMatch;
import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.handler.MongoMarketHandler;
import com.wikex.wikex.coinswap.handler.NettyHandler;
import com.wikex.wikex.coinswap.handler.WebsocketMarketHandler;
import com.wikex.wikex.coinswap.job.ExchangePushJob;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.coinswap.service.ContractMarketService;
import com.wikex.wikex.coinswap.service.ContractOrderEntrustCoinService;
import com.wikex.wikex.coinswap.service.MemberContractWalletCoinService;
import com.wikex.wikex.coinswap.socket.client.WsClientHuobi;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ContractCoinMatchStarter implements ApplicationRunner {

    @Autowired
    private Client client0;

    @Autowired
    private Client client1;

    @Autowired
    private ContractCoinCoinService contractCoinService;

    @Autowired
    private ContractMarketService marketService;

    @Autowired
    private ExchangePushJob exchangePushJob;

    @Autowired
    private ContractOrderEntrustCoinService contractOrderEntrusdtService;

    @Autowired
    private MemberTransactionFeign memberTransactionFeign;
    @Autowired
    private MemberContractWalletCoinService memberContractWalletService;


    @Autowired
    MongoMarketHandler mongoMarketHandler;

    @Autowired
    WebsocketMarketHandler wsHandler;

    @Autowired
    NettyHandler nettyHandler;
    @Autowired
    SnowflakeConfig snowflakeConfig;

    @Autowired
    private ContractCoinMatchFactory factory;

    @Value("${platformCoins}")
    private String platformCoins;

    @Value("${coinswap.platform.wsUrl}")
    private String platformWsUrl;
    @Value("${coinswap.huobi.wsUrl}")
    private String huobiWsUrl;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        
        List<String> platformCoinList =  Arrays.stream(platformCoins.split(",")).collect(Collectors.toList());

        
        List<ContractCoinCoin> contractCoinList = contractCoinService.findAllEnabled();

        for(ContractCoinCoin coin : contractCoinList) {
            ContractCoinMatch match = new ContractCoinMatch(coin.getSymbol());
            match.setContractCoinService(contractCoinService);
            match.setContractOrderEntrustService(contractOrderEntrusdtService);
            match.setMemberTransactionFeign(memberTransactionFeign);
            match.setMemberContractWalletService(memberContractWalletService);
            match.addHandler(mongoMarketHandler);
            match.addHandler(wsHandler);
            match.addHandler(nettyHandler);
            match.setExchangePushJob(exchangePushJob);
            match.setSnowflakeConfig(snowflakeConfig);
            match.run();
            factory.addContractCoinMatch(coin.getSymbol(), match);
        }

        

        
        WikexWebSocketConnectionManage.setClient(client0);
        HuobiWebSocketConnectionManage.setClient(client1);

        WsClientHuobi w = new WsClientHuobi(factory);
        w.setContractCoinCoinService(contractCoinService);
        w.setContractMarketService(marketService);
        w.setExchangePushJob(exchangePushJob);
        w.setPlatformCoins(platformCoinList);
        w.setWsUrl(huobiWsUrl);
        w.run();








    }
}
