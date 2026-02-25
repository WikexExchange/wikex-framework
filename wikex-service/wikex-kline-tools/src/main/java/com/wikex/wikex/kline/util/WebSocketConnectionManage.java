package com.wikex.wikex.kline.util;

import com.wikex.wikex.kline.client.Client;
import com.wikex.wikex.kline.socket.ws.WebSocketHuobi;

public class WebSocketConnectionManage {

    private static Client client;
    public static WebSocketHuobi ws; 
    public static Client getClient() { return client; }
    public static void setClient(Client client) {
        WebSocketConnectionManage.client = client;
    }

    public static WebSocketHuobi getWebSocket() { return ws; }
    public static void setWebSocket(WebSocketHuobi ws) { WebSocketConnectionManage.ws = ws; }
}
