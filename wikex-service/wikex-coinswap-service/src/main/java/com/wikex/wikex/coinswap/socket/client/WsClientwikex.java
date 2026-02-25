package com.wikex.wikex.coinswap.socket.client;

import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.job.ExchangePushJob;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.coinswap.service.ContractMarketService;

import com.wikex.wikex.coinswap.socket.ws.WebSocketwikex;
import com.wikex.wikex.coinswap.util.WikexWebSocketConnectionManage;
import com.wikex.wikex.util.ProxyUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class WsClientwikex {

    private ContractCoinCoinService contractCoinCoinService;

    private ContractMarketService marketService;

    private ExchangePushJob exchangePushJob;

    private ContractCoinMatchFactory matchFactory;

    private List<String> platformCoins;

    private String wsUrl;

    public WsClientwikex(ContractCoinMatchFactory factory) {
        this.matchFactory = factory;
    }

    public void run() {


        try {
            // wss://api.huobi.pro/ws   ws://api.huobi.br.com:443/ws  wss://api.huobiasia.vip/ws
            URI uri = new URI(wsUrl);
            WebSocketwikex ws = new WebSocketwikex(uri, matchFactory, marketService, exchangePushJob,platformCoins);
            ws.setProxy(ProxyUtil.getProxy());
            WikexWebSocketConnectionManage.setWebSocket(ws);
            WikexWebSocketConnectionManage.getClient().connect(ws);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setContractCoinCoinService(ContractCoinCoinService service) {
        this.contractCoinCoinService = service;
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
