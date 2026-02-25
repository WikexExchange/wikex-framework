package com.wikex.wikex.coinswap.test;

import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.util.ProxyUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.zip.GZIPInputStream;

/**
 *  Huobi WebSocket Kline data fetcher
 */
public class WebsocketClient {

    public static WebSocketClient client;

    /* Main method */
    public static void main(String[] args) throws InterruptedException {
        try {
            client = new WebSocketClient(new URI("wss://api.huobi.pro/ws"), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    System.out.println("<<<Handshake - Success>>>");
                }
                @Override
                public void onMessage(String msg) {
                    System.out.println("<<<Received - Message>>>" + msg);
                }
                @Override
                public void onMessage(ByteBuffer bytes) {
                    try {
                        String message = new String(ZipUtil.decompressGzipData(bytes.array()), "UTF-8"); /* Decompress GZIP */
                        JSONObject jsonObject = JSONObject.parseObject(message); /* Convert to JSON */
                        if (null != jsonObject.getString("ping")) { /* Check if it's a Ping */
                            System.out.println("Received message: " + message); /* Received Ping */
                            JSONObject json = new JSONObject();
                            json.put("pong", getEpochSeconds());
                            System.out.println("Sending message: " + json.toString());
                            send(json.toString()); /* Send Pong response */
                        } else {
                            System.err.println("Received message: " + message);
                            JSONObject tick = jsonObject.getJSONObject("tick");
                            Double p = tick.getDouble("close");
                            System.out.println("===》 " + p + " 《===");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onClose(int i, String s, boolean b) {
                    System.out.println("<<<Connection - Closed>>>");
                }
                @Override
                public void onError(Exception e){
                    e.printStackTrace();
                    System.out.println("<<<Error - Closed>>>");
                }
                /* Get current epoch seconds */
                long getEpochSeconds() {
                    return Instant.now().getEpochSecond();
                }
            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        client.setProxy(ProxyUtil.getProxy());

        client.connect();
        System.out.println("Connecting...");
        while(!client.getReadyState().equals(ReadyState.OPEN)){
            System.out.print(".");
            Thread.sleep(500);
        }
        System.out.println();

        /* Connected successfully, send subscription message */
        JSONObject json = new JSONObject();
        json.put("sub","market.btcusdt.kline.1min");
        json.put("id","id1");
        System.out.println("Sending message: " + json.toString());
        client.send(json.toString());
    }
}

/* Zip utility class */
class ZipUtil {
    public static byte[] decompressGzipData(byte[] depressData) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(depressData);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GZIPInputStream gis = new GZIPInputStream(is);
        int count;
        byte data[] = new byte[1024];
        while ((count = gis.read(data, 0, 1024)) != -1) {
            os.write(data, 0, count);
        }
        gis.close();
        depressData = os.toByteArray();
        os.flush();
        os.close();
        is.close();
        return depressData;
    }
}
