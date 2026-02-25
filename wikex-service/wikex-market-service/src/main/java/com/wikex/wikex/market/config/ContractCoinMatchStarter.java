package com.wikex.wikex.market.config;

import com.wikex.wikex.market.client.Client;
import com.wikex.wikex.market.service.KlineRobotMarketService;
import com.wikex.wikex.market.socket.client.WsClientHuobi;
import com.wikex.wikex.market.util.WebSocketConnectionManage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ContractCoinMatchStarter implements ApplicationRunner {

    private Logger logger = LoggerFactory.getLogger(ContractCoinMatchStarter.class);

    @Autowired
    private Client client;
    @Autowired
    private KlineRobotMarketService klineRobotMarketService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            WebSocketConnectionManage.setClient(client);
            WsClientHuobi w = new WsClientHuobi();
            w.setContractMarketService(klineRobotMarketService);
            w.run();
        } catch (Exception e) {
            logger.error("Critical error ", e);
        }
    }
}
