package com.wikex.wikex.coinswap.socket.client;



import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.job.ExchangePushJob;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.coinswap.service.ContractMarketService;
import com.wikex.wikex.coinswap.socket.ws.WebSocketHuobi;
import com.wikex.wikex.coinswap.util.HuobiWebSocketConnectionManage;
import com.wikex.wikex.util.ProxyUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class WsClientHuobi {

    private ContractCoinCoinService contractCoinService;

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
            ws.setProxy(ProxyUtil.getProxy());
            HuobiWebSocketConnectionManage.setWebSocket(ws);
            HuobiWebSocketConnectionManage.getClient().connect(ws);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setContractCoinCoinService(ContractCoinCoinService service) {
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
