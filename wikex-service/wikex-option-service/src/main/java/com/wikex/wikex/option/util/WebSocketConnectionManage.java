package com.wikex.wikex.option.util;


import com.wikex.wikex.option.client.Client;
import com.wikex.wikex.option.socket.ws.WebSocketHuobi;

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
