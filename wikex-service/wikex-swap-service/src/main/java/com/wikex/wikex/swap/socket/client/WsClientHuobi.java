package com.wikex.wikex.swap.socket.client;



import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.job.ExchangePushJob;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.ContractMarketService;
import com.wikex.wikex.swap.socket.ws.WebSocketHuobi;
import com.wikex.wikex.swap.util.HuobiWebSocketConnectionManage;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class WsClientHuobi {

    private ContractCoinService contractCoinService;

    private ContractMarketService marketService;

    private ExchangePushJob exchangePushJob;

    private ContractCoinMatchFactory matchFactory;

    private List<String> platformCoins;

    private String wsUrl;

    public WsClientHuobi(ContractCoinMatchFactory factory) {
        this.matchFactory = factory;
    }

    public void run() {
        try {
            // wss://api.huobi.pro/ws   ws://api.huobi.br.com:443/ws  wss://api.huobiasia.vip/ws
            URI uri = new URI(wsUrl);
            WebSocketHuobi ws = new WebSocketHuobi(uri, matchFactory, marketService, exchangePushJob,platformCoins);
            HuobiWebSocketConnectionManage.setWebSocket(ws);
            HuobiWebSocketConnectionManage.getClient().connect(ws);

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
