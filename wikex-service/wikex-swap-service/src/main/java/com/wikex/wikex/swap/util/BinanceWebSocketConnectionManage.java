package com.wikex.wikex.swap.util;


import com.wikex.wikex.swap.client.Client;
import com.wikex.wikex.swap.socket.ws.WebSocketBinance;

public class BinanceWebSocketConnectionManage {

    private static Client client;
    public static WebSocketBinance ws; 
    public static Client getClient() { return client; }
    public static void setClient(Client client) {
        BinanceWebSocketConnectionManage.client = client;
    }

    public static WebSocketBinance getWebSocket() { return ws; }
    public static void setWebSocket(WebSocketBinance ws) { BinanceWebSocketConnectionManage.ws = ws; }
}
