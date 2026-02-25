package com.wikex.wikex.second.socket.client;


import com.wikex.wikex.second.engine.ContractCoinMatchFactory;
import com.wikex.wikex.second.entity.ContractSecondCoin;
import com.wikex.wikex.second.job.ExchangePushJob;
import com.wikex.wikex.second.service.ContractMarketService;
import com.wikex.wikex.second.service.ContractSecondCoinService;
import com.wikex.wikex.second.socket.ws.WebSocketHuobi;
import com.wikex.wikex.second.util.WebSocketConnectionManage;
import com.wikex.wikex.util.ProxyUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class WsClientHuobi {

    private ContractSecondCoinService contractSecondCoinService;

    private ContractMarketService marketService;

    private ExchangePushJob exchangePushJob;

    private ContractCoinMatchFactory matchFactory;

    public WsClientHuobi(ContractCoinMatchFactory factory) {
        this.matchFactory = factory;
    }

    public void run() {

        try {
            URI uri = new URI("wss://api.huobi.pro/ws");
            WebSocketHuobi ws = new WebSocketHuobi(uri, matchFactory, marketService, exchangePushJob);
            ws.setProxy(ProxyUtil.getProxy());
            WebSocketConnectionManage.setWebSocket(ws);
            WebSocketConnectionManage.getClient().connect(ws);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setContractSecondCoinService(ContractSecondCoinService service) {
        this.contractSecondCoinService = service;
    }
    public void setContractMarketService(ContractMarketService service) { this.marketService = service; }
    public void setExchangePushJob(ExchangePushJob pushJob) { this.exchangePushJob = pushJob; }
}
