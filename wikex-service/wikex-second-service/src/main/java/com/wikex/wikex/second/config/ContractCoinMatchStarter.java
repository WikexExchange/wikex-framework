package com.wikex.wikex.second.config;

import com.wikex.wikex.second.client.Client;
import com.wikex.wikex.second.engine.ContractCoinMatch;
import com.wikex.wikex.second.engine.ContractCoinMatchFactory;
import com.wikex.wikex.second.entity.ContractSecondCoin;
import com.wikex.wikex.second.handler.MongoMarketHandler;
import com.wikex.wikex.second.handler.NettyHandler;
import com.wikex.wikex.second.handler.WebsocketMarketHandler;
import com.wikex.wikex.second.job.ExchangePushJob;
import com.wikex.wikex.second.service.ContractMarketService;
import com.wikex.wikex.second.service.ContractSecondCoinService;
import com.wikex.wikex.second.service.ContractSecondOrderService;
import com.wikex.wikex.second.socket.client.WsClientHuobi;
import com.wikex.wikex.second.util.WebSocketConnectionManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractCoinMatchStarter implements ApplicationRunner {

    private Logger log = LoggerFactory.getLogger(ContractCoinMatchStarter.class);

    @Autowired
    private Client client;

    @Autowired
    private ContractSecondCoinService contractSecondCoinService;

    @Autowired
    private ContractMarketService marketService;

    @Autowired
    private ExchangePushJob exchangePushJob;


    @Autowired
    private ContractSecondOrderService contractSecondOrderService;

    @Autowired
    MongoMarketHandler mongoMarketHandler;

    @Autowired
    WebsocketMarketHandler wsHandler;

    @Autowired
    NettyHandler nettyHandler;

    @Autowired
    private ContractCoinMatchFactory factory;

    @Override
    public void run(ApplicationArguments args){

        
        List<ContractSecondCoin> contractCoinList = contractSecondCoinService.findAllEnabled();

        for(ContractSecondCoin coin : contractCoinList) {
            ContractCoinMatch match = new ContractCoinMatch(coin.getSymbol());
            match.setContractSecondCoinService(contractSecondCoinService);
            match.setContractSecondOrderService(contractSecondOrderService);
            match.addHandler(mongoMarketHandler);
            match.addHandler(wsHandler);
            match.addHandler(nettyHandler);
            match.setExchangePushJob(exchangePushJob);
            match.run();
            factory.addContractCoinMatch(coin.getSymbol(), match);
        }

        

        WebSocketConnectionManage.setClient(client);

        WsClientHuobi w = new WsClientHuobi(factory);
        w.setContractSecondCoinService(contractSecondCoinService);
        w.setContractMarketService(marketService);
        w.setExchangePushJob(exchangePushJob);
        w.run();
    }
}
