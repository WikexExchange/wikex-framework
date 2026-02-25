package com.wikex.wikex.second.socket.ws;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.pojo.*;
import com.wikex.wikex.second.engine.ContractCoinMatchFactory;
import com.wikex.wikex.second.job.ExchangePushJob;
import com.wikex.wikex.second.service.ContractMarketService;
import com.wikex.wikex.second.util.ZipUtils;
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
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHuobi extends WebSocketClient {

    private Logger logger = LoggerFactory.getLogger(WebSocketClient.class);
    private ArrayList<String> subCoinList = new ArrayList<String>();
    private ConcurrentHashMap<String,String> symbolMap = new ConcurrentHashMap<>();

    private ContractCoinMatchFactory matchFactory;
    private ContractMarketService marketService;
    private ExchangePushJob exchangePushJob;

    public static String DEPTH = "market.%s.depth.step0"; // Depth
    public static String KLINE = "market.%s.kline.%s"; // K-line
    public static String DETAIL = "market.%s.detail"; // Market overview (latest price, volume, etc.)
    public static String TRADE = "market.%s.trade.detail"; // Trade details

    private double VOLUME_PERCENT = 0.13; // Percentage of Huobi trading volume

    public static String PERIOD[] = { "1min", "5min", "15min", "30min", "60min","4hour", "1day", "1mon", "1week" };

    public WebSocketHuobi(URI uri, ContractCoinMatchFactory matchFactory, ContractMarketService service, ExchangePushJob pushJob) {
        super(uri);
        this.uri = uri;
        this.matchFactory = matchFactory;
        this.marketService = service;
        this.exchangePushJob = pushJob;
    }

    @Override
    public void onOpen(ServerHandshake shake) {
        logger.info("[WebSocketHuobi] Start price WebSocket listening...");
        if (null != this.matchFactory.getMatchMap() && this.matchFactory.getMatchMap().size() > 0) {
            for(String symbol : this.matchFactory.getMatchMap().keySet()) {
                if(!subCoinList.contains(symbol)){
                    subCoinList.add(symbol);
                }
                symbolMap.put(symbol.replace("/", "").toLowerCase(),symbol);
                // Subscribe depth
                String depthTopic = String.format(DEPTH, symbol.replace("/", "").toLowerCase());
                logger.info("[WebSocketHuobi][" + symbol + "] Depth subscription: " + depthTopic);
                sendWsMarket("sub", depthTopic);

                // Subscribe market overview
                String detailTopic = String.format(DETAIL, symbol.replace("/", "").toLowerCase());
                logger.info("[WebSocketHuobi][" + symbol + "] Overview subscription: " + detailTopic);
                sendWsMarket("sub", detailTopic);

                // Subscribe trade details
                String tradeTopic = String.format(TRADE, symbol.replace("/", "").toLowerCase());
                logger.info("[WebSocketHuobi][" + symbol + "] Trade details subscription: " + tradeTopic);
                sendWsMarket("sub", tradeTopic);

                // Subscribe realtime K-line
//                for(String period : PERIOD) {
//                    String klineTopic = String.format(KLINE, symbol.replace("/", "").toLowerCase(), period);
//                    logger.info("[WebSocketHuobi][" + symbol + "] Realtime K-line subscription: " + klineTopic);
//                    sendWsMarket("sub", klineTopic);
//                }
            }
        }
    }

    /**
     * Subscribe to a newly-added symbol's market streams
     * @param symbol
     */
    public void subNewCoin(String symbol) {
        if(!subCoinList.contains(symbol)){
            subCoinList.add(symbol);

            String detailTopic = String.format(DETAIL, symbol.replace("/", "").toLowerCase());
            logger.info("[WebSocketHuobi][" + symbol + "] Overview subscription: " + detailTopic);
            sendWsMarket("sub", detailTopic);

            String tradeTopic = String.format(TRADE, symbol.replace("/", "").toLowerCase());
            logger.info("[WebSocketHuobi][" + symbol + "] Trade details subscription: " + tradeTopic);
            sendWsMarket("sub", tradeTopic);

            String depthTopic = String.format(DEPTH, symbol.replace("/", "").toLowerCase());
            logger.info("[WebSocketHuobi][" + symbol + "] Depth subscription: " + depthTopic);
            sendWsMarket("sub", depthTopic);
        }
    }

    // Sync K-lines
    public void reqKLineList(String symbol, String period, long from, long to) {
        String topic = String.format(KLINE, symbol.replace("/", "").toLowerCase(), period);

        // Huobi WebSocket requires each request to contain at most 300 bars, so we need to split requests
        long timeGap = to - from; // time difference
        long divideTime = 0;
        if(period.equals("1min")) divideTime = 60 * 300; // 1 minute * 300 bars
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
        // Try 20 times
        logger.info("[WebSocketHuobi] Attempt to reconnect, attempt " + tryTimes);
        if(this.getReadyState().equals(ReadyState.NOT_YET_CONNECTED) || this.getReadyState().equals(ReadyState.CLOSED) || this.getReadyState().equals(ReadyState.CLOSING)) {

            Runnable sendable = new Runnable() {
                @Override
                public void run() {
                    logger.info("[WebSocketHuobi] Start reconnecting");
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
                    String symbol = symbolMap.get(sb.toString());
                    if(symbol==null || "".equals(symbol)){
                        return;
                    }
                    symbol=symbol.toUpperCase();
                    String type = id.split("\\.")[2];

                    if(type.equals("kline")) {

                        String data = jsonObject.getString("data");
                        String period = id.split("\\.")[3];

                        if (null != data && !"".equals(data) && JSONUtils.isJsonArray(data)) {

                            List<Poke> pokes = marketService.findPokeAndRemove(symbol,type,period);
                            BigDecimal lowPokePrice = null;
                            BigDecimal highPokePrice = null;
                            if(pokes!=null && pokes.size()>0){
                                for (Poke poke : pokes) {
                                    BigDecimal price = BigDecimal.valueOf(Double.parseDouble(poke.getPrice()));
                                    if(lowPokePrice == null){
                                        lowPokePrice = price;
                                    }else {
                                        if(lowPokePrice.compareTo(price)==1){
                                            lowPokePrice = price; // take the lowest price
                                        }
                                    }

                                    if(highPokePrice == null){
                                        highPokePrice = price;
                                    }else {
                                        if(highPokePrice.compareTo(price)==-1){
                                            highPokePrice = price; // take the highest price
                                        }
                                    }
                                }
                            }

                            JSONArray klineList = jsonObject.getJSONArray("data");

                            for(int i = 0; i < klineList.size(); i++) {
                                JSONObject klineObj = klineList.getJSONObject(i);

                                BigDecimal open = klineObj.getBigDecimal("open"); // open price
                                BigDecimal close = klineObj.getBigDecimal("close"); // close price
                                BigDecimal high =  klineObj.getBigDecimal("high"); // high
                                BigDecimal low =  klineObj.getBigDecimal("low"); // low
                                if(i==klineList.size()-1) {
                                    high = highPokePrice == null ? klineObj.getBigDecimal("high") : highPokePrice.compareTo(klineObj.getBigDecimal("high")) == 1 ? highPokePrice : klineObj.getBigDecimal("high"); // high
                                    low = lowPokePrice == null ? klineObj.getBigDecimal("low") : lowPokePrice.compareTo(klineObj.getBigDecimal("low")) == -1 ? lowPokePrice : klineObj.getBigDecimal("low"); // low
                                }
                                BigDecimal amount = klineObj.getBigDecimal("amount"); // amount
                                BigDecimal vol = klineObj.getBigDecimal("vol"); // volume

                                int count = klineObj.getIntValue("count"); // count
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

                                // Push K-line (if only one bar, it's the latest and should be pushed to frontend)
                                if(klineList.size() == 1) {
                                    //logger.info("K-line push: " + kline.getPeriod() + " - " + symbol + " - " + kline.getTime());
                                    exchangePushJob.pushTickKline(symbol, kline);
                                }else if(klineList.size() > 1){
                                    if(i == klineList.size() - 1) {
                                        //logger.info("K-line push: " + kline.getPeriod() + " - " + symbol + " - " + kline.getTime()");
                                        exchangePushJob.pushTickKline(symbol, kline);
                                    }
                                }
                            }
                        }
                    }else if(type.equals("depth")){
                        String tick = jsonObject.getString("tick");
                        if (null != tick && !"".equals(tick) && JSONUtils.isJsonObject(tick)) {


                            JSONObject plateObj = JSONObject.parseObject(tick);

                            // Bid depth
                            JSONArray bids = plateObj.getJSONArray("bids");
                            List<TradePlateItem> buyItems = new ArrayList<>();
                            for(int i = 0; i < bids.size(); i++) {
                                TradePlateItem item = new TradePlateItem();
                                JSONArray itemObj = bids.getJSONArray(i);
                                item.setPrice(itemObj.getBigDecimal(0));
                                item.setAmount(itemObj.getBigDecimal(1));
                                buyItems.add(item);
                            }

                            // Ask depth
                            JSONArray asks = plateObj.getJSONArray("asks");
                            List<TradePlateItem> sellItems = new ArrayList<>();
                            for(int i = 0; i < asks.size(); i++) {
                                TradePlateItem item = new TradePlateItem();
                                JSONArray itemObj = asks.getJSONArray(i);
                                item.setPrice(itemObj.getBigDecimal(0));
                                item.setAmount(itemObj.getBigDecimal(1));
                                sellItems.add(item);
                            }
                            // Refresh order book data
                            this.matchFactory.getContractCoinMatch(symbol).refreshPlate(buyItems, sellItems);

                            //logger.info("[WebSocketHuobi] Order book updated: {} bids, {} asks", bids.size(), asks.size());
                        }
                    }else if(type.equals("detail")){ // Market overview
                        String tick = jsonObject.getString("tick");
                        if (null != tick && !"".equals(tick) && JSONUtils.isJsonObject(tick)) {
                            JSONObject detailObj = JSONObject.parseObject(tick);
                            List<Poke> pokes = marketService.findPokeAndRemove(symbol,type,null);
                            BigDecimal lowPokePrice = null;
                            BigDecimal highPokePrice = null;
                            if(pokes!=null && pokes.size()>0){
                                for (Poke poke : pokes) {
                                    BigDecimal price = BigDecimal.valueOf(Double.parseDouble(poke.getPrice()));
                                    if(lowPokePrice == null && detailObj.getBigDecimal("low").compareTo(price)==1){
                                        lowPokePrice = price;
                                    }else if(lowPokePrice!=null){
                                        if(detailObj.getBigDecimal("high") .compareTo(price)==1){
                                            lowPokePrice = price; // take the lowest price
                                        }
                                    }

                                    if(highPokePrice == null && detailObj.getBigDecimal("high").compareTo(price)==-1){
                                        highPokePrice = price;
                                    }else if(highPokePrice!=null){
                                        if(highPokePrice.compareTo(price)==-1){
                                            highPokePrice = price; // take the highest price
                                        }
                                    }
                                }
                            }

                            BigDecimal amount = detailObj.getBigDecimal("amount");
                            BigDecimal open = detailObj.getBigDecimal("open");
                            BigDecimal close = detailObj.getBigDecimal("close");
                            BigDecimal high = highPokePrice==null ? detailObj.getBigDecimal("high") : highPokePrice.compareTo(detailObj.getBigDecimal("high"))==1?highPokePrice:detailObj.getBigDecimal("high");
                            BigDecimal count = detailObj.getBigDecimal("count");
                            BigDecimal low = lowPokePrice==null ? detailObj.getBigDecimal("low") : lowPokePrice.compareTo(detailObj.getBigDecimal("low"))==-1?lowPokePrice:detailObj.getBigDecimal("low");
                            BigDecimal vol = detailObj.getBigDecimal("vol");

                            CoinThumb thumb = new CoinThumb();
                            thumb.setOpen(open);
                            thumb.setClose(close);
                            thumb.setHigh(high);
                            thumb.setLow(low);
                            thumb.setVolume(amount.multiply(BigDecimal.valueOf(VOLUME_PERCENT))); // volume
                            thumb.setTurnover(vol.multiply(BigDecimal.valueOf(VOLUME_PERCENT))); // turnover
                            this.matchFactory.getContractCoinMatch(symbol).refreshThumb(thumb);

                            // Order trigger or liquidation
                            if(lowPokePrice == null && highPokePrice == null){
                                this.matchFactory.getContractCoinMatch(symbol).refreshPrice(close);
                            }else {
                                if(lowPokePrice!=null){
                                    this.matchFactory.getContractCoinMatch(symbol).refreshPrice(lowPokePrice);
                                    logger.info("Bi-directional liquidation check second-contract price lowPokePrice:{}, time:{}", lowPokePrice , System.currentTimeMillis());
                                }
                                if(highPokePrice!=null){
                                    this.matchFactory.getContractCoinMatch(symbol).refreshPrice(highPokePrice);
                                    logger.info("Bi-directional liquidation check second-contract price highPokePrice:{}, time:{}", highPokePrice , System.currentTimeMillis());
                                }
                            }
                            logger.info("[WebSocketHuobi] {}, price updated: {}", symbol,close);
                        }
                    }else if(type.equals("trade")) { // Trade details
                        String tick = jsonObject.getString("tick");
                        if (null != tick && !"".equals(tick) && JSONUtils.isJsonObject(tick)) {
                            List<Poke> pokes = marketService.findPokeAndRemove(symbol,type,null);
                            JSONObject detailObj = JSONObject.parseObject(tick);
                            JSONArray tradeList = detailObj.getJSONArray("data");
                            List<ContractTrade> tradeArrayList = new ArrayList<ContractTrade>();
                            for(int i = 0; i < tradeList.size(); i++) {
                                BigDecimal amount = tradeList.getJSONObject(i).getBigDecimal("amount");
                                BigDecimal price = tradeList.getJSONObject(i).getBigDecimal("price");
                                String direction = tradeList.getJSONObject(i).getString("direction");
                                long time = tradeList.getJSONObject(i).getLongValue("ts");
                                String tradeId = tradeList.getJSONObject(i).getString("tradeId");

                                // Create trade
                                ContractTrade trade = new ContractTrade();
                                trade.setAmount(amount);
                                trade.setPrice(price);
                                if(direction.equals("buy")) {
                                    trade.setDirection(ContractOrderDirection.BUY);
                                    trade.setBuyOrderId(tradeId);
                                    trade.setBuyTurnover(amount.multiply(price));
                                }else{
                                    trade.setDirection(ContractOrderDirection.SELL);
                                    trade.setSellOrderId(tradeId);
                                    trade.setSellTurnover(amount.multiply(price));
                                }
                                trade.setSymbol(symbol);
                                trade.setTime(time);

                                tradeArrayList.add(trade);
                            }

                            if(pokes!=null && pokes.size()>0){
                                for (int i = 0; i < pokes.size(); i++) {
                                    int index = i;
                                    if(i>=tradeList.size()-1){
                                        index = tradeList.size()-1;
                                    }
                                    BigDecimal amount = tradeList.getJSONObject(index).getBigDecimal("amount");
                                    String direction = tradeList.getJSONObject(index).getString("direction");
                                    long time = tradeList.getJSONObject(index).getLongValue("ts");
                                    String tradeId = tradeList.getJSONObject(index).getString("tradeId")+i;
                                    BigDecimal price = BigDecimal.valueOf(Double.parseDouble(pokes.get(i).getPrice()));
                                    // Create trade
                                    ContractTrade trade = new ContractTrade();
                                    trade.setAmount(amount);
                                    trade.setPrice(price);
                                    if(direction.equals("buy")) {
                                        trade.setDirection(ContractOrderDirection.BUY);
                                        trade.setBuyOrderId(tradeId);
                                        trade.setBuyTurnover(amount.multiply(price));
                                    }else{
                                        trade.setDirection(ContractOrderDirection.SELL);
                                        trade.setSellOrderId(tradeId);
                                        trade.setSellTurnover(amount.multiply(price));
                                    }
                                    trade.setSymbol(symbol);
                                    trade.setTime(time);
                                    tradeArrayList.add(trade);
                                }
                            }
                            // Refresh recent trades
                            this.matchFactory.getContractCoinMatch(symbol).refreshLastedTrade(tradeArrayList);

                            logger.info("[WebSocketHuobi] {} trade details updated: total {}",symbol, tradeArrayList.size());
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
