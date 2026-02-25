package com.wikex.wikex.swap.client;

import com.alibaba.fastjson.JSON;
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
import java.util.*;
import java.util.zip.GZIPInputStream;

public class WebsocketClient {

    public static WebSocketClient client;
    public static String DEPTH = "%s@depth@100ms";
    public static String KLINE = "%s@kline_1m";
    public static String DETAIL = "%s@miniTicker";
    public static String TRADE = "%s@trade";
    public static Map<String, Map<String, String>> bMap;
    public static Map<String, Map<String, String>> aMap;

    public static void main(String[] args) throws InterruptedException {
        try {
            bMap = new HashMap<>();
            aMap = new HashMap<>();
            client = new WebSocketClient(new URI("wss://stream.binance.com:443/ws/btcusdt@miniTicker"),
                    new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    // System.out.println("<<<Handshake - Success>>>");
                    List<String> subCoinList = new ArrayList<>();

                    subCoinList.add("EOS/USDT");
                    List<String> topics = new ArrayList<>();
                    for (String symbol : subCoinList) {
                        String depthTopic = String.format(DEPTH, symbol.replace("/", "").toLowerCase());
                        topics.add(depthTopic);

                        String detailTopic = String.format(DETAIL, symbol.replace("/", "").toLowerCase());
                        topics.add(detailTopic);

                        String tradeTopic = String.format(TRADE, symbol.replace("/", "").toLowerCase());
                        topics.add(tradeTopic);
                    }
                    sendWsMarket("SUBSCRIBE", topics);
                }

                @Override
                public void onMessage(String msg) {
                    JSONObject jsonObject = JSON.parseObject(msg);
                    String e = jsonObject.getString("e");
                    if ("depthUpdate".equals(e)) {
                        Cryptocurrency cy = JSON.parseObject(msg, Cryptocurrency.class);
                        List<List<String>> bidList = cy.getB();
                        List<List<String>> askList = cy.getA();
                        Map<String, String> bM = bMap.get(cy.getS());
                        Map<String, String> aM = aMap.get(cy.getS());
                        if (bM == null) {
                            bM = new HashMap<>();
                        }
                        if (aM == null) {
                            aM = new HashMap<>();
                        }
                        for (List<String> list : bidList) {
                            bM.put(list.get(0), list.get(1));
                        }
                        for (List<String> list : askList) {
                            aM.put(list.get(0), list.get(1));
                        }
                        bMap.put(cy.getS(), bM);
                        aMap.put(cy.getS(), aM);
                        // System.out.println(JSON.toJSONString(bMap));
                    }
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    try {
                        // Decompress GZIP data
                        String message = new String(ZipUtil.unzipGZIPData(bytes.array()), "UTF-8");
                        JSONObject jsonObject = JSONObject.parseObject(message);

                        // Check if it's a Ping
                        if (null != jsonObject.getString("ping")) {
                            // System.out.println("Received message: " + message);
                            JSONObject json = new JSONObject();
                            json.put("pong", getEpochSeconds());
                            // System.out.println("Sending message: " + json.toString());
                            send(json.toString()); // Send Pong response
                        }
//                        else {
//                            // System.err.println("Received message: " + message);
//                            // JSONObject tick = jsonObject.getJSONObject("tick");
//                            // Double p = tick.getDouble("close");
//                            // System.out.println("===》 " + p + " 《===");
//                        }
                    } catch (Exception e) {
                        // e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    // System.out.println("<<<Connection Closed>>>");
                }

                @Override
                public void onError(Exception e) {
                    // e.printStackTrace();
                    // System.out.println("<<<Error Occurred - Connection Closed>>>");
                }

                long getEpochSeconds() {
                    return Instant.now().getEpochSecond();
                }
            };
        } catch (URISyntaxException e) {
            // e.printStackTrace();
        }

        client.setProxy(ProxyUtil.getProxy());

        client.connect();
        // System.out.println("Connecting...");
        while (!client.getReadyState().equals(ReadyState.OPEN)) {
            // System.out.print(".");
            Thread.sleep(500);
        }

        // Connection established, ready to send info
    }

    public static void sendWsMarket(String op, List<String> topics) {
        JSONObject req = new JSONObject();
        req.put("method", op);
        req.put("params", topics);
        client.send(req.toString());
    }
}

/* Zip Utility Class */
class ZipUtil {
    public static byte[] unzipGZIPData(byte[] depressData) throws Exception {
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

class Cryptocurrency {
    private String e;
    private Long E;
    private String s;
    private Long U;
    private Long u;
    private List<List<String>> b;
    private List<List<String>> a;

    public String getE() {
        return e;
    }

    public void setE(Long e) {
        E = e;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public Long getU() {
        return U;
    }

    public void setU(Long u) {
        U = u;
    }

    public List<List<String>> getB() {
        return b;
    }

    public void setB(List<List<String>> b) {
        this.b = b;
    }

    public List<List<String>> getA() {
        return a;
    }

    public void setA(List<List<String>> a) {
        this.a = a;
    }

    public void setE(String e) {
        this.e = e;
    }
}
