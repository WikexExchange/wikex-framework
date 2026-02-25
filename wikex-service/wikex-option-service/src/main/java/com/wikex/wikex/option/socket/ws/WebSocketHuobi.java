package com.wikex.wikex.option.socket.ws;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOptionOrderDirection;
import com.wikex.wikex.option.engine.ContractOptionCoinMatchFactory;
import com.wikex.wikex.option.job.ExchangePushJob;
import com.wikex.wikex.option.service.ContractMarketService;
import com.wikex.wikex.option.util.ZipUtils;
import com.wikex.wikex.pojo.*;
import com.wikex.wikex.util.JSONUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.List;

public class WebSocketHuobi extends WebSocketClient {

    private Logger logger = LoggerFactory.getLogger(WebSocketClient.class);
    private ArrayList<String> subCoinList = new ArrayList<String>();

    private ContractOptionCoinMatchFactory matchFactory;
    private ContractMarketService marketService;
    private ExchangePushJob exchangePushJob;

    public static String DEPTH = "market.%s.depth.step0"; 
    public static String KLINE = "market.%s.kline.%s"; 
    public static String DETAIL = "market.%s.detail"; 
    public static String TRADE = "market.%s.trade.detail"; 

    private double VOLUME_PERCENT = 0.13; 

    public static String PERIOD[] = { "1min", "5min", "15min", "30min", "60min","4hour", "1day", "1mon", "1week" };

    public WebSocketHuobi(URI uri, ContractOptionCoinMatchFactory matchFactory, ContractMarketService service, ExchangePushJob pushJob) {
        super(uri);
        this.uri = uri;
        this.matchFactory = matchFactory;
        this.marketService = service;
        this.exchangePushJob = pushJob;
    }

    @Override
  public void onOpen(ServerHandshake shake) {
    logger.info("[WebSocketHuobi] Starting price WebSocket listener...");
    if (null != this.matchFactory.getMatchMap() && this.matchFactory.getMatchMap().size() > 0) {
        for(String symbol : this.matchFactory.getMatchMap().keySet()) {
            if(!subCoinList.contains(symbol)){
                subCoinList.add(symbol);
            }
            




            
            String detailTopic = String.format(DETAIL, symbol.replace("/", "").toLowerCase());
            logger.info("[WebSocketHuobi][" + symbol + "] Overview subscription: " + detailTopic);
            sendWsMarket("sub", detailTopic);


                




                





            }
        }
    }

    
    public void subNewCoinPrice(String symbol) {
        if(!subCoinList.contains(symbol)){
            subCoinList.add(symbol);
            
            for (String period : PERIOD) {
                String topic = String.format(KLINE, symbol.replace("/", "").toLowerCase(), period);
                logger.info("[WebSocketHuobi][" + symbol + "] K-line subscription: " + topic);

                sendWsMarket("sub", topic);
            }
        }
    }

    
    public void subNewCoinDepth(String symbol) {
        if(!subCoinList.contains(symbol)){
            subCoinList.add(symbol);
            
            String topic = String.format(DEPTH, symbol.replace("/", "").toLowerCase());
          logger.info("[WebSocketHuobi][" + symbol + "] Depth subscription: " + topic);

            sendWsMarket("sub", topic);
        }
    }

    
    public void reqKLineList(String symbol, String period, long from, long to) {
        String topic = String.format(KLINE, symbol.replace("/", "").toLowerCase(), period);

        
        long timeGap = to - from; 
        long divideTime = 0;
        if(period.equals("1min")) divideTime = 60 * 300; 
        if(period.equals("5min")) divideTime = 5* 60 * 300;
        if(period.equals("15min")) divideTime = 15* 60 * 300;
        if(period.equals("30min")) divideTime = 30 * 60 * 300;
        if(period.equals("60min")) divideTime = 60 * 60 * 300;
        if(period.equals("4hour")) divideTime = 4 * 60 * 60 * 300;
        if(period.equals("1day")) divideTime = 24 * 60 * 60 * 300;
        if(period.equals("1week")) divideTime = 7 * 24 * 60 * 60 * 300;
        if(period.equals("1mon")) divideTime = 30 * 24 * 60 * 60 * 300;

        if(timeGap > divideTime) {
            long times = timeGap % (divideTime) > 0 ?  (timeGap/(divideTime) + 1) : timeGap/(divideTime);
            long temTo = from;
            long temFrom = from;
            for(int i = 0; i < times; i++) {
                if(temTo + (divideTime) > to) {
                    temTo = to;
                }else{
                    temTo = temTo + (divideTime);
                }
                sendWsMarket("req", topic, temFrom, temTo);
                temFrom = temFrom + divideTime;
            }
        }else{
            sendWsMarket("req", topic, from, to);
        }
    }

    @Override
    public void onMessage(String arg0) {
        if (arg0 != null) {
            logger.info("[WebSocketHuobi] receive message: {}", arg0);
        }
    }

    @Override
    public void onError(Exception arg0) {
        logger.error("[WebSocketHuobi] has error ,the message is :: {}", arg0.getMessage());
        arg0.printStackTrace();
        String message = "";
        try {
            message = new String(arg0.getMessage().getBytes(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[WebSocketHuobi] has error ,the message is :: {}", message);
        }
    }

    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
        logger.info("[WebSocketHuobi] connection close: {} - {} - {}", arg0, arg1, arg2);
        int tryTimes = 0;
        
       logger.info("[WebSocketHuobi] Attempting to reconnect, attempt " + tryTimes);
       if (this.getReadyState().equals(ReadyState.NOT_YET_CONNECTED) || this.getReadyState().equals(ReadyState.CLOSED) || this.getReadyState().equals(ReadyState.CLOSING)) {

        Runnable sendable = new Runnable() {
         @Override
         public void run() {
             logger.info("[WebSocketHuobi] Starting reconnection");
             reconnect();
         }
     };
    new Thread(sendable).start();


        }
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        try {
            String message = new String(ZipUtils.decompress(bytes.array()), "UTF-8");

            JSONObject jsonObject = JSONObject.parseObject(message);
            if (!"".equals(message)) {
                if (message.indexOf("ping") > 0) {
                    String pong = jsonObject.toString();
                    send(pong.replace("ping", "pong"));
                } else {

                    String id = "";
                    if(jsonObject.containsKey("ch")) {
                        id = jsonObject.getString("ch");
                        if (id == null || id.split("\\.").length < 3) {
                            return;
                        }
                    }
                    if(jsonObject.containsKey("rep")) {
                        id = jsonObject.getString("rep");
                        if (id == null || id.split("\\.").length < 3) {
                            return;
                        }
                    }
                    if(id.equals("")) {
                        return;
                    }
                    StringBuilder sb = new StringBuilder(id.split("\\.")[1]);
                    String symbol = sb.insert(sb.indexOf("usdt"), "/").toString().toUpperCase();

                    String type = id.split("\\.")[2];

                    if(type.equals("kline")) {

                        String data = jsonObject.getString("data");
                        String period = id.split("\\.")[3];

                        if (null != data && !"".equals(data) && JSONUtils.isJsonArray(data)) {

                            PresetPrice presetPrice = marketService.findPresetPrice(symbol,type);
                            BigDecimal price = presetPrice==null ? null : BigDecimal.valueOf(Double.parseDouble(presetPrice.getPrice()));

                            JSONArray klineList = jsonObject.getJSONArray("data");

                            for(int i = 0; i < klineList.size(); i++) {
                                JSONObject klineObj = klineList.getJSONObject(i);

                                BigDecimal open = (price==null || i==0)? klineObj.getBigDecimal("open") : price; 
                                BigDecimal close = (price==null || i==klineList.size()-1) ? klineObj.getBigDecimal("close") : price; 
                                BigDecimal high = price==null ? klineObj.getBigDecimal("high") : price; 
                                BigDecimal low = price==null ? klineObj.getBigDecimal("low") : price; 
                                BigDecimal amount = klineObj.getBigDecimal("amount"); 
                                BigDecimal vol = klineObj.getBigDecimal("vol"); 
                                int count = klineObj.getIntValue("count"); 
                                long time = klineObj.getLongValue("id");

                                KLine kline = new KLine(period);
                                kline.setClosePrice(close);
                                kline.setCount(count);
                                kline.setHighestPrice(high);
                                kline.setLowestPrice(low);
                                kline.setOpenPrice(open);
                                kline.setTime(time);
                                kline.setTurnover(amount.multiply(BigDecimal.valueOf(VOLUME_PERCENT)));
                                kline.setVolume(vol.multiply(BigDecimal.valueOf(VOLUME_PERCENT)));
                                marketService.saveKLine(symbol, kline);

                                
                                if(klineList.size() == 1) {
                                    exchangePushJob.pushTickKline(symbol, kline);
                                }
                            }
                        }
                    }else if(type.equals("depth")){
                        String tick = jsonObject.getString("tick");
                        if (null != tick && !"".equals(tick) && JSONUtils.isJsonObject(tick)) {

                            PresetPrice presetPrice = marketService.findPresetPrice(symbol,type);
                            BigDecimal price = presetPrice==null ? null : BigDecimal.valueOf(Double.parseDouble(presetPrice.getPrice()));

                            JSONObject plateObj = JSONObject.parseObject(tick);

                            
                            JSONArray bids = plateObj.getJSONArray("bids");
                            List<TradePlateItem> buyItems = new ArrayList<>();
                            for(int i = 0; i < bids.size(); i++) {
                                TradePlateItem item = new TradePlateItem();
                                JSONArray itemObj = bids.getJSONArray(i);
                                item.setPrice(price==null ? itemObj.getBigDecimal(0) : price);
                                item.setAmount(itemObj.getBigDecimal(1));
                                buyItems.add(item);
                            }

                            
                            JSONArray asks = plateObj.getJSONArray("asks");
                            List<TradePlateItem> sellItems = new ArrayList<>();
                            for(int i = 0; i < asks.size(); i++) {
                                TradePlateItem item = new TradePlateItem();
                                JSONArray itemObj = asks.getJSONArray(i);
                                item.setPrice(price==null ? itemObj.getBigDecimal(0) : price);
                                item.setAmount(itemObj.getBigDecimal(1));
                                sellItems.add(item);
                            }
                            
                            this.matchFactory.getContractCoinMatch(symbol).refreshPlate(buyItems, sellItems);

                            
                        }
                    }else if(type.equals("detail")){ 
                        String tick = jsonObject.getString("tick");
                        if (null != tick && !"".equals(tick) && JSONUtils.isJsonObject(tick)) {
                            JSONObject detailObj = JSONObject.parseObject(tick);
                            PresetPrice presetPrice = marketService.findPresetPrice(symbol,type);
                            BigDecimal price = presetPrice==null ? null : BigDecimal.valueOf(Double.parseDouble(presetPrice.getPrice()));

                            BigDecimal amount = detailObj.getBigDecimal("amount");
                            BigDecimal open = price==null ? detailObj.getBigDecimal("open") : price;
                            BigDecimal close = price==null ? detailObj.getBigDecimal("close") : price;
                            BigDecimal high = price==null ? detailObj.getBigDecimal("high") : price;
                            BigDecimal count = detailObj.getBigDecimal("count");
                            BigDecimal low = price==null ? detailObj.getBigDecimal("low") : price;
                            BigDecimal vol = detailObj.getBigDecimal("vol");


                            CoinThumb thumb = new CoinThumb();
                            thumb.setOpen(open);
                            thumb.setClose(close);
                            thumb.setHigh(high);
                            thumb.setLow(low);
                            thumb.setVolume(amount.multiply(BigDecimal.valueOf(VOLUME_PERCENT))); 
                            thumb.setTurnover(vol.multiply(BigDecimal.valueOf(VOLUME_PERCENT))); 
                            this.matchFactory.getContractCoinMatch(symbol).refreshThumb(thumb);

                            
                            this.matchFactory.getContractCoinMatch(symbol).refreshPrice(close);
                            
                        }
                    }else if(type.equals("trade")) { 
                        String tick = jsonObject.getString("tick");
                        if (null != tick && !"".equals(tick) && JSONUtils.isJsonObject(tick)) {
                            JSONObject detailObj = JSONObject.parseObject(tick);
                            PresetPrice presetPrice = marketService.findPresetPrice(symbol,type);
                            BigDecimal preset = presetPrice==null ? null : BigDecimal.valueOf(Double.parseDouble(presetPrice.getPrice()));

                            JSONArray tradeList = detailObj.getJSONArray("data");
                            List<ContractOptionTrade> tradeArrayList = new ArrayList<ContractOptionTrade>();
                            for(int i = 0; i < tradeList.size(); i++) {
                                BigDecimal amount = tradeList.getJSONObject(i).getBigDecimal("amount");
                                BigDecimal price = preset==null ? tradeList.getJSONObject(i).getBigDecimal("price") : preset;
                                String direction = tradeList.getJSONObject(i).getString("direction");
                                long time = tradeList.getJSONObject(i).getLongValue("ts");
                                String tradeId = tradeList.getJSONObject(i).getString("tradeId");

                                
                                ContractOptionTrade trade = new ContractOptionTrade();
                                trade.setAmount(amount);
                                trade.setPrice(price);
                                if(direction.equals("buy")) {
                                    trade.setDirection(ContractOptionOrderDirection.BUY);
                                    trade.setBuyOrderId(tradeId);
                                    trade.setBuyTurnover(amount.multiply(price));
                                }else{
                                    trade.setDirection(ContractOptionOrderDirection.SELL);
                                    trade.setSellOrderId(tradeId);
                                    trade.setSellTurnover(amount.multiply(price));
                                }
                                trade.setSymbol(symbol);
                                trade.setTime(time);

                                tradeArrayList.add(trade);

                                
                                this.matchFactory.getContractCoinMatch(symbol).refreshLastedTrade(tradeArrayList);
                            }

                            
                        }
                    }
                }
            }
        } catch (CharacterCodingException e) {
            e.printStackTrace();
            logger.error("[WebSocketHuobi] websocket exception: {}", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[WebSocketHuobi] websocket exception: {}", e.getMessage());
        }
    }

    public void sendWsMarket(String op, String topic) {
        JSONObject req = new JSONObject();
        req.put(op, topic);
        send(req.toString());
    }

    public void sendWsMarket(String op, String topic, long from, long to) {
        JSONObject req = new JSONObject();
        req.put(op, topic);
        req.put("from", from);
        req.put("to", to);
        send(req.toString());
    }
}
