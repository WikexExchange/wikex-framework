package com.wikex.wikex.coinswap.util;


import com.wikex.wikex.coinswap.client.Client;
import com.wikex.wikex.coinswap.socket.ws.WebSocketwikex;

public class WikexWebSocketConnectionManage {

    private static Client client;
    public static WebSocketwikex ws; 
    public static Client getClient() { return client; }
    public static void setClient(Client client) {
        WikexWebSocketConnectionManage.client = client;
    }

    public static WebSocketwikex getWebSocket() { return ws; }
    public static void setWebSocket(WebSocketwikex ws) { WikexWebSocketConnectionManage.ws = ws; }
}
