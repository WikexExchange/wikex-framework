package com.wikex.wikex.swap.socket.client;


import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.job.ExchangePushJob;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.ContractMarketService;
import com.wikex.wikex.swap.socket.ws.WebSocketBinance;
import com.wikex.wikex.swap.util.BinanceWebSocketConnectionManage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class WsClientBinance {

    private ContractCoinService contractCoinService;

    private ContractMarketService marketService;

    private ExchangePushJob exchangePushJob;

    private ContractCoinMatchFactory matchFactory;

    private List<String> platformCoins;

    private String wsUrl;

    public WsClientBinance(ContractCoinMatchFactory factory) {
        this.matchFactory = factory;
    }

    public void run() {
        try {
            
            URI uri = new URI(wsUrl);
            WebSocketBinance ws = new WebSocketBinance(uri, matchFactory, marketService, exchangePushJob,platformCoins);
            BinanceWebSocketConnectionManage.setWebSocket(ws);
            BinanceWebSocketConnectionManage.getClient().connect(ws);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setContractCoinService(ContractCoinService service) {
        this.contractCoinService = service;
    }
    public void setContractMarketService(ContractMarketService service) { this.marketService = service; }
    public void setExchangePushJob(ExchangePushJob pushJob) { this.exchangePushJob = pushJob; }

    public void setPlatformCoins(List<String> platformCoins){
       this.platformCoins = platformCoins;
    }

    public void setWsUrl(String wsUrl){
        this.wsUrl = wsUrl;
    }
}
