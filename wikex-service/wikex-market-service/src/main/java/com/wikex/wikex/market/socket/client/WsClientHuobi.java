package com.wikex.wikex.market.socket.client;


import com.wikex.wikex.market.service.KlineRobotMarketService;
import com.wikex.wikex.market.socket.ws.WebSocketHuobi;
import com.wikex.wikex.market.util.WebSocketConnectionManage;
import com.wikex.wikex.util.ProxyUtil;

import java.net.URI;
import java.net.URISyntaxException;

public class WsClientHuobi {


    public WsClientHuobi() {}
    private KlineRobotMarketService marketService;

    public void run() {

        try {
            URI uri = new URI("wss://api.huobi.pro/ws");
            WebSocketHuobi ws = new WebSocketHuobi(uri,  marketService);
            ws.setProxy(ProxyUtil.getProxy());
            WebSocketConnectionManage.setWebSocket(ws);
            WebSocketConnectionManage.getClient().connect(ws);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    public void setContractMarketService(KlineRobotMarketService service) { this.marketService = service; }
}
