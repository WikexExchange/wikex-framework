package com.wikex.wikex.kline.socket.client;

import com.wikex.wikex.kline.service.KlineRobotMarketService;
import com.wikex.wikex.kline.socket.ws.WebSocketHuobi;
import com.wikex.wikex.kline.util.WebSocketConnectionManage;

import java.net.URI;
import java.net.URISyntaxException;

public class WsClientHuobi {


    public WsClientHuobi() {}
    private KlineRobotMarketService marketService;

    public void run() {

        try {
            // wss://api.huobi.pro/ws   ws://api.huobi.br.com:443/ws  wss://api.huobiasia.vip/ws
            URI uri = new URI("wss://api.huobi.pro/ws");
            WebSocketHuobi ws = new WebSocketHuobi(uri,  marketService);
            WebSocketConnectionManage.setWebSocket(ws);
            WebSocketConnectionManage.getClient().connect(ws);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    public void setContractMarketService(KlineRobotMarketService service) { this.marketService = service; }
}
