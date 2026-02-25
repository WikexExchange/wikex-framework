package com.wikex.wikex.swap.config;

import com.wikex.wikex.config.SnowflakeConfig;
import com.wikex.wikex.swap.client.Client;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.handler.MongoMarketHandler;
import com.wikex.wikex.swap.handler.NettyHandler;
import com.wikex.wikex.swap.handler.WebsocketMarketHandler;
import com.wikex.wikex.swap.job.ExchangePushJob;
import com.wikex.wikex.swap.service.*;
import com.wikex.wikex.swap.socket.client.WsClientBinance;
import com.wikex.wikex.swap.socket.client.WsClientHuobi;
import com.wikex.wikex.swap.util.WikexWebSocketConnectionManage;
import com.wikex.wikex.swap.util.HuobiWebSocketConnectionManage;
import com.wikex.wikex.swap.util.BinanceWebSocketConnectionManage;
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

//    @Autowired
//    private Client client1;

    @Autowired
    private ContractCoinService contractCoinService;

    @Autowired
    private ContractMarketService marketService;

    @Autowired
    private ExchangePushJob exchangePushJob;

    @Autowired
    private ContractOrderEntrustService contractOrderEntrusdtService;

    @Autowired
    private MemberTransactionFeign memberTransactionFeign;
    @Autowired
    private MemberContractWalletService memberContractWalletService;

    @Autowired
    private MemberTradeLimitService memberTradeLimitService;
    @Autowired
    private MemberContractPositionService memberContractPositionService;

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

    @Value("${swap.platform.wsUrl}")
    private String platformWsUrl;
    @Value("${swap.huobi.wsUrl}")
    private String huobiWsUrl;

    @Value("${swap.binance.wsUrl}")
    private String binanceWsUrl;
//wss://stream.binance.com:443/ws/btcusdt@miniTicker


    @Override
    public void run(ApplicationArguments args) throws Exception {
        
        List<String> platformCoinList =  Arrays.stream(platformCoins.split(",")).collect(Collectors.toList());

        
        List<ContractCoin> contractCoinList = contractCoinService.findAllEnabled();

        for(ContractCoin coin : contractCoinList) {
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
            match.setSnowflakeConfig(snowflakeConfig);
            match.setMemberContractPositionService(memberContractPositionService);
            match.run();
            factory.addContractCoinMatch(coin.getSymbol(), match);
        }

        

//        BinanceWebSocketConnectionManage.setClient(client0);
        HuobiWebSocketConnectionManage.setClient(client0);

//        WsClientBinance w = new WsClientBinance(factory);
//        w.setContractCoinService(contractCoinService);
//        w.setContractMarketService(marketService);
//        w.setExchangePushJob(exchangePushJob);
//        w.setPlatformCoins(platformCoinList);
//        w.setWsUrl(binanceWsUrl);
//        w.run();

        WsClientHuobi w = new WsClientHuobi(factory);
        w.setContractCoinService(contractCoinService);
        w.setContractMarketService(marketService);
        w.setExchangePushJob(exchangePushJob);
        w.setPlatformCoins(platformCoinList);
        w.setWsUrl(huobiWsUrl);
        w.run();


//
//        WsClientwikex b = new WsClientwikex(factory);
//        b.setContractCoinService(contractCoinService);
//        b.setContractMarketService(marketService);
//        b.setExchangePushJob(exchangePushJob);
//        b.setPlatformCoins(platformCoinList);
//        b.setWsUrl(platformWsUrl);
//        b.run();
    }
}
