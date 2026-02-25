package com.wikex.wikex.coinswap.util;


import com.wikex.wikex.coinswap.client.Client;
import com.wikex.wikex.coinswap.socket.ws.WebSocketHuobi;

public class HuobiWebSocketConnectionManage {

    private static Client client;
    public static WebSocketHuobi ws; 
    public static Client getClient() { return client; }
    public static void setClient(Client client) {
        HuobiWebSocketConnectionManage.client = client;
    }

    public static WebSocketHuobi getWebSocket() { return ws; }
    public static void setWebSocket(WebSocketHuobi ws) { HuobiWebSocketConnectionManage.ws = ws; }
}
