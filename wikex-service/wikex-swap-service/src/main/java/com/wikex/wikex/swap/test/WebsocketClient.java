package com.wikex.wikex.swap.test;

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
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class WebsocketClient {

    public static WebSocketClient client;

    /* Main method */
    public static void main(String[] args) throws InterruptedException {
        try {
            client = new WebSocketClient(
                    new URI("wss://fstream.binance.com/ws/btcusdt@miniTicker"),
                    new Draft_6455()
            ) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    System.out.println("<<<Handshake - Success>>>");
                }

                @Override
                public void onMessage(String msg) {
                    System.out.println("<<<Received Message>>> " + msg);
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    try {
                        String message = new String(ZipUtil.unzipGZIPData(bytes.array()), "UTF-8"); // Unzip GZIP
                        JSONObject jsonObject = JSONObject.parseObject(message); // Parse JSON
                        if (null != jsonObject.getString("ping")) { // Check if Ping
                            System.out.println("Received message: " + message); // Received Ping
                            JSONObject json = new JSONObject();
                            json.put("pong", getEpochSeconds());
                            System.out.println("Send message: " + json.toString());
                            send(json.toString()); // Send Pong
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
                    System.out.println("<<<Connection Closed>>>");
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    System.out.println("<<<Error>>>");
                }

                long getEpochSeconds() {
                    return Instant.now().getEpochSecond();
                }
            };
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        client.setProxy(ProxyUtil.getProxy());

        client.connect();
        System.out.println("Connecting");
        while (!client.getReadyState().equals(ReadyState.OPEN)) {
            System.out.print(".");
            Thread.sleep(500);
        }
        System.out.println();

        // Subscribe to BTC/USDT miniTicker
        List<String> topics = new ArrayList<>();
        String detailTopic = String.format("%s@miniTicker", "BTC/USDT".replace("/", "").toLowerCase());
        topics.add(detailTopic);
        JSONObject req = new JSONObject();
        req.put("method", "SUBSCRIBE");
        req.put("params", topics);
        client.send(req.toString());

        /* Example: send subscription to Huobi format (disabled here) */
        // JSONObject json = new JSONObject();
        // json.put("sub", "market.btcusdt.kline.1min");
        // json.put("id", "id1");
        // System.out.println("Send message: " + json.toString());
        // client.send(json.toString());
    }
}

/* Zip Utility */
class ZipUtil {
    public static byte[] unzipGZIPData(byte[] depressData) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(depressData);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        GZIPInputStream gis = new GZIPInputStream(is);
        int count;
        byte[] data = new byte[1024];
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
