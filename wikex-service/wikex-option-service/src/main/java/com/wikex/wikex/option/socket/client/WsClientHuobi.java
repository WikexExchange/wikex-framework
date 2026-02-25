package com.wikex.wikex.option.socket.client;

import com.wikex.wikex.option.engine.ContractOptionCoinMatchFactory;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.wikex.wikex.option.job.ExchangePushJob;
import com.wikex.wikex.option.service.ContractMarketService;
import com.wikex.wikex.option.service.ContractOptionCoinService;
import com.wikex.wikex.option.socket.ws.WebSocketHuobi;
import com.wikex.wikex.option.util.WebSocketConnectionManage;
import com.wikex.wikex.util.ProxyUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class WsClientHuobi {

    private ContractOptionCoinService contractOptionCoinService;

    private ContractMarketService marketService;

    private ExchangePushJob exchangePushJob;

    private ContractOptionCoinMatchFactory matchFactory;

    public WsClientHuobi(ContractOptionCoinMatchFactory factory) {
        this.matchFactory = factory;
    }

    public void run() {

        List<ContractOptionCoin> contractOptionCoinList = contractOptionCoinService.findAll();

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

    public void setContractOptionCoinService(ContractOptionCoinService service) {
        this.contractOptionCoinService = service;
    }
    public void setContractMarketService(ContractMarketService service) { this.marketService = service; }
    public void setExchangePushJob(ExchangePushJob pushJob) { this.exchangePushJob = pushJob; }
}
