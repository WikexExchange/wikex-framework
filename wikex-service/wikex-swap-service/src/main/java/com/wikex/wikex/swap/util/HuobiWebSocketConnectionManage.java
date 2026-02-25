package com.wikex.wikex.swap.util;


import com.wikex.wikex.swap.client.Client;
import com.wikex.wikex.swap.socket.ws.WebSocketHuobi;

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
