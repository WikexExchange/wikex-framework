package com.wikex.wikex.swap.socket.ws;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.pojo.*;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.job.ExchangePushJob;
import com.wikex.wikex.swap.service.ContractMarketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;

@Slf4j
public class WebSocketBinance extends WebSocketClient{

    private ArrayList<String> subCoinList = new ArrayList<String>();

    private ContractCoinMatchFactory matchFactory;
    private ContractMarketService marketService;
    private ExchangePushJob exchangePushJob;

    private List<String> platformCoins;

    public static String DEPTH = "%s@depth@1000ms"; // Order book depth
    public static String KLINE = "%s@kline_1m"; // K-line (candlesticks)
    public static String DETAIL = "%s@miniTicker"; // Market overview (latest price, volume, etc.)
    public static String TRADE = "%s@aggTrade"; // Trade details

    private double VOLUME_PERCENT = 0.13; // Percentage of Huobi trading volume

    public static String PERIOD[] = { "1min", "5min", "15min", "30min", "60min","4hour", "1day", "1mon", "1week" };
    public static Map<String,LimitedSizeTreeMap<BigDecimal,BigDecimal>> bMap = new HashMap<>();
    public static Map<String,LimitedSizeTreeMap<BigDecimal,BigDecimal>> aMap = new HashMap<>();

    private static String  klineUrl = "https://fapi.binance.com/fapi/v1/klines";
    public WebSocketBinance(URI uri, ContractCoinMatchFactory matchFactory, ContractMarketService service, ExchangePushJob pushJob, List<String> platformCoins) {
        super(uri);
        this.uri = uri;
        this.matchFactory = matchFactory;
        this.marketService = service;
        this.exchangePushJob = pushJob;
        this.platformCoins=platformCoins;
    }

    @Override
    public void onOpen(ServerHandshake shake) {
        // 
        if (null != this.matchFactory.getMatchMap() && this.matchFactory.getMatchMap().size() > 0) {
            // Initialize subCoinList
            for(String symbol : this.matchFactory.getMatchMap().keySet()) {
                // Non-platform coin
                if(!platformCoins.contains(symbol.split("/")[0]) && !platformCoins.contains(symbol.split("/")[1])){
                    if(!subCoinList.contains(symbol)){
                        subCoinList.add(symbol);
                    }
                }
            }
            List<String> topics = new ArrayList<>();
            for(String symbol : subCoinList) {
                // Subscribe to order book depth
                String depthTopic = String.format(DEPTH, symbol.replace("/", "").toLowerCase());
                // 
                topics.add(depthTopic);

                // Subscribe to market overview
                String detailTopic = String.format(DETAIL, symbol.replace("/", "").toLowerCase());
                // 
                topics.add(detailTopic);

                // Subscribe to trade details
                String tradeTopic = String.format(TRADE, symbol.replace("/", "").toLowerCase());
                // 
                topics.add( tradeTopic);
            }
            sendWsMarket("SUBSCRIBE", topics);
        }
    }

    /**
     * Subscribe to market information for a new trading pair
     * @param symbol
     */
    public void subNewCoin(String symbol) {
        List<String> topics = new ArrayList<>();
        if(!subCoinList.contains(symbol)){
            subCoinList.add(symbol);
            String detailTopic = String.format(DETAIL, symbol.replace("/", "").toLowerCase());
            // 
            topics.add(detailTopic);

            String tradeTopic = String.format(TRADE, symbol.replace("/", "").toLowerCase());
            // 
            topics.add(tradeTopic);

            String depthTopic = String.format(DEPTH, symbol.replace("/", "").toLowerCase());
            // 
            topics.add(depthTopic);
        }
        sendWsMarket("SUBSCRIBE", topics);
    }

    // Synchronize K-line
    public void reqKLineList(String symbol, String period, long from, long to) {
        // Binance WebSocket requires that a single request cannot exceed 300 items, so the request needs to be split
        long timeGap = to - from; // Time difference
        long divideTime = 0;
        if(period.equals("1min")) divideTime = 60 * 300; // 1 minute * 300 items
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
                requestKline( symbol,period, temFrom, temTo);
                temFrom = temFrom + divideTime;
            }
        }else{
            requestKline( symbol,period, from, to);
        }
    }

    @Override
    public void onMessage(String message) {
        try {
            if (StringUtils.isEmpty(message)) {
                return;
            }

            JSONObject jsonObject = JSON.parseObject(message);
            String type = jsonObject.getString("e");
            if ("kline".equals(type)) {
                System.out.println("Not listening");
            } else if ("depthUpdate".equals(type)) {
                Cryptocurrency cy = JSON.parseObject(message, Cryptocurrency.class);
                StringBuilder sb = new StringBuilder(cy.getS());
                String symbol = sb.insert(sb.indexOf("USDT"), "/").toString().toUpperCase();
                List<List<String>> bidList = cy.getB();
                List<List<String>> askList = cy.getA();
                LimitedSizeTreeMap<BigDecimal, BigDecimal> bM = bMap.get(symbol);
                LimitedSizeTreeMap<BigDecimal, BigDecimal> aM = aMap.get(symbol);
                if(bM == null){
                    bM = new LimitedSizeTreeMap<>(Comparator.reverseOrder());
                }
                if(aM == null){
                    aM = new LimitedSizeTreeMap<>();
                }
                for (List<String> list : bidList) {
                    bM.put(new BigDecimal(list.get(0)),new BigDecimal(list.get(1)));
                }
                for (List<String> list : askList) {
                    aM.put(new BigDecimal(list.get(0)),new BigDecimal(list.get(1)));
                }
                bMap.put(symbol,bM);
                aMap.put(symbol,aM);
                List<TradePlateItem> buyItems = new ArrayList<>();
                List<TradePlateItem> sellItems = new ArrayList<>();
                
                if(bM!=null){
                    Set<BigDecimal> keys = bM.keySet();
                    for (BigDecimal key : keys) {
                        TradePlateItem item = new TradePlateItem();
                        item.setPrice(key);
                        item.setAmount(bM.get(key));
                        buyItems.add(item);
                    }
                }
                if(aM!=null){
                    Set<BigDecimal> keys = aM.keySet();
                    for (BigDecimal key : keys) {
                        TradePlateItem item = new TradePlateItem();
                        item.setPrice(key);
                        item.setAmount(aM.get(key));
                        sellItems.add(item);
                    }
                }
                // Refresh order book data
                this.matchFactory.getContractCoinMatch(symbol).refreshPlate(buyItems, sellItems);

            } else if ("24hrMiniTicker".equals(type)) { // Market overview
                StringBuilder sb = new StringBuilder(jsonObject.getString("s"));
                String symbol = sb.insert(sb.indexOf("USDT"), "/").toString().toUpperCase();

                List<Poke> pokes = marketService.findPokeAndRemove(symbol, type, null);
                BigDecimal lowPokePrice = null;
                BigDecimal highPokePrice = null;
                if (pokes != null && pokes.size() > 0) {
                    for (Poke poke : pokes) {
                        BigDecimal price = BigDecimal.valueOf(Double.parseDouble(poke.getPrice()));
                        if (lowPokePrice == null) {
                            lowPokePrice = price;
                        } else if (lowPokePrice != null) {
                            if (lowPokePrice.compareTo(price) == 1) {
                                lowPokePrice = price; // take the lowest price
                            }
                        }

                        if (highPokePrice == null) {
                            highPokePrice = price;
                        } else if (highPokePrice != null) {
                            if (highPokePrice.compareTo(price) == -1) {
                                highPokePrice = price; // take the highest price
                            }
                        }
                    }
                }
                    BigDecimal amount = jsonObject.getBigDecimal("v");
                    BigDecimal open = jsonObject.getBigDecimal("o");
                    BigDecimal close = jsonObject.getBigDecimal("c");
                    BigDecimal high = highPokePrice == null ? jsonObject.getBigDecimal("h") : highPokePrice.compareTo(jsonObject.getBigDecimal("h")) == 1 ? highPokePrice : jsonObject.getBigDecimal("h");
                    BigDecimal low = lowPokePrice == null ? jsonObject.getBigDecimal("l") : lowPokePrice.compareTo(jsonObject.getBigDecimal("l")) == -1 ? lowPokePrice : jsonObject.getBigDecimal("l");
                    BigDecimal vol = jsonObject.getBigDecimal("q");

                    CoinThumb thumb = new CoinThumb();
                    thumb.setOpen(open);
                    thumb.setClose(close);
                    thumb.setHigh(high);
                    thumb.setLow(low);
                    thumb.setVolume(amount.multiply(BigDecimal.valueOf(VOLUME_PERCENT))); // Trading volume
                    thumb.setTurnover(vol.multiply(BigDecimal.valueOf(VOLUME_PERCENT))); // Turnover
                    this.matchFactory.getContractCoinMatch(symbol).refreshThumb(thumb);
                    if (lowPokePrice != null && highPokePrice != null && lowPokePrice.compareTo(highPokePrice) == 0) {
                        highPokePrice = null; // if lowPokePrice == highPokePrice, remove one
                    }
                    

                    // Trigger order or liquidation
                    if (lowPokePrice == null && highPokePrice == null) {
                        this.matchFactory.refreshPrice(symbol,close);
                    } else {
                        if (lowPokePrice != null) {
                            this.matchFactory.refreshPrice(symbol,lowPokePrice);
                            // 
                        }
                        if (highPokePrice != null) {
                            this.matchFactory.refreshPrice(symbol,highPokePrice);
                            // 
                        }

                    }
                    

            } else if ("aggTrade".equals(type)) { // Trade details
                StringBuilder sb = new StringBuilder(jsonObject.getString("s"));
                String symbol = sb.insert(sb.indexOf("USDT"), "/").toString().toUpperCase();

                List<Poke> pokes = marketService.findPokeAndRemove(symbol, type, null);
                List<ContractTrade> tradeArrayList = new ArrayList<ContractTrade>();
                BigDecimal amount = jsonObject.getBigDecimal("q");
                BigDecimal price = jsonObject.getBigDecimal("p");
                Boolean direction = jsonObject.getBoolean("m");
                long time = jsonObject.getLongValue("E");
                String tradeId = jsonObject.getString("a");

                // Create trade
                ContractTrade trade = new ContractTrade();
                trade.setAmount(amount);
                trade.setPrice(price);
                if (!direction) {
                    trade.setDirection(ContractOrderDirection.BUY);
                    trade.setBuyOrderId(tradeId);
                    trade.setBuyTurnover(amount.multiply(price));
                } else {
                    trade.setDirection(ContractOrderDirection.SELL);
                    trade.setSellOrderId(tradeId);
                    trade.setSellTurnover(amount.multiply(price));
                }
                trade.setSymbol(symbol);
                trade.setTime(time);

                tradeArrayList.add(trade);

                if (pokes != null && pokes.size() > 0) {
                    for (int i = 0; i < pokes.size(); i++) {
                        ContractTrade tradePoke = new ContractTrade();
                        BeanUtil.copyProperties(trade,tradePoke);
                        // Create trade
                        tradePoke.setPrice(BigDecimal.valueOf(Double.parseDouble(pokes.get(i).getPrice())));
                        if (!direction) {
                            tradePoke.setDirection(ContractOrderDirection.BUY);
                            tradePoke.setBuyOrderId(tradeId+i);
                            tradePoke.setBuyTurnover(amount.multiply(price));
                        } else {
                            tradePoke.setDirection(ContractOrderDirection.SELL);
                            tradePoke.setSellOrderId(tradeId+i);
                            tradePoke.setSellTurnover(amount.multiply(price));
                        }
                        // 
                        tradeArrayList.add(tradePoke);
                        // Push trade poke
                        List<ContractTrade> ts = new ArrayList<>();
                        ts.add(tradePoke);
                        exchangePushJob.pushTickTrade(symbol,ts);
                    }
                }
                // Refresh trade records
                this.matchFactory.getContractCoinMatch(symbol).refreshLastedTrade(tradeArrayList);
                // 
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Exception arg0) {
        log.error("[WebSocketBinance] has error ,the message is :: {}", arg0.getMessage());
        arg0.printStackTrace();
        String message = "";
        try {
            message = new String(arg0.getMessage().getBytes(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[WebSocketBinance] has error ,the message is :: {}", message);
        }
    }

    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
        // 
        int tryTimes = 0;
        // Try 20 times
        // 
        if(this.getReadyState().equals(ReadyState.NOT_YET_CONNECTED) || this.getReadyState().equals(ReadyState.CLOSED) || this.getReadyState().equals(ReadyState.CLOSING)) {

            Runnable sendable = new Runnable() {
                @Override
                public void run() {
                    // 
                    reconnect();
                }
            };
            new Thread(sendable).start();
        }
    }

    @Override
    public void onMessage(ByteBuffer bytes) {

    }

    public void sendWsMarket(String op, List<String> topics) {
        JSONObject req = new JSONObject();
        req.put("method", op);;
        req.put("params",topics);
        // req.put("id",1);
        send(req.toString());
    }
    public void sendWsMarket(String op, String topic, long from, long to) {
        JSONObject req = new JSONObject();
        req.put(op, topic);
        req.put("from", from);
        req.put("to", to);
        send(req.toString());
    }

    public void requestKline(String symbol, String period, long from, long to) {
        String interval = "1m";
        if(period.equals("1min")) {
            interval = "1m";// 1 minute * 300 items
        }
        if(period.equals("5min")) {
            interval = "5m";// 1 minute * 300 items
        }
        if(period.equals("15min")) {
            interval = "15m";// 1 minute * 300 items
        }
        if(period.equals("30min")) {
            interval = "30m";// 1 minute * 300 items
        }
        if(period.equals("60min")) {
            interval = "1h";// 1 minute * 300 items
        }
        if(period.equals("4hour")) {
            interval = "4h";// 1 minute * 300 items
        }
        if(period.equals("1day")) {
            interval = "1d";// 1 minute * 300 items
        }
        if(period.equals("1week")) {
            interval = "1w";// 1 minute * 300 items
        }
        if(period.equals("1mon")) {
            interval = "1M";// 1 minute * 300 items
        }
        from = from * 1000;
        to = to * 1000;
        String symbolTemp = symbol.replace("/","").toUpperCase();
        String url = klineUrl + "?symbol="+symbolTemp+"&interval="+interval+"&startTime="+from+"&endTime="+to+"&limit=1000";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> result = restTemplate.getForEntity(url,  String.class);
        // 
        long timeStamp = marketService.findMaxTimestamp(symbol.toUpperCase(), period);
        BigDecimal lowPokePrice = null;
        BigDecimal highPokePrice = null;
        List<JSONArray>  klineList = JSON.parseObject(result.getBody(),List.class);

        KLine lastKline = null;
        long lastTime = 0;
        for(int i = 0; i < klineList.size(); i++) {
            JSONArray klineObj = klineList.get(i);
            long time = klineObj.getLongValue(0);
            lastTime = lastTime > time ? lastTime : time;
        }
        if(lastTime > timeStamp * 1000){
            List<Poke> pokes = marketService.findPokeAndRemove(symbol,"kline",period);
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
        }
        List<KLine> needPushList = new LinkedList<>();
        for(int i = 0; i < klineList.size(); i++) {
            JSONArray klineObj = klineList.get(i);
            KLine kline = new KLine(period);
            BigDecimal open = klineObj.getBigDecimal(1) ; // Close price
            BigDecimal close = klineObj.getBigDecimal(4) ;// Close price
            BigDecimal high = klineObj.getBigDecimal(2);
            BigDecimal low = klineObj.getBigDecimal(3);
            BigDecimal amount = klineObj.getBigDecimal(7); // Close price
            BigDecimal vol = klineObj.getBigDecimal(5); // Close price
            int count = klineObj.getIntValue(8); // Close price
            long time = klineObj.getLongValue(0)/1000;

            kline.setClosePrice(close);
            kline.setCount(count);
            kline.setHighestPrice(high);
            kline.setLowestPrice(low);
            kline.setOpenPrice(open);
            kline.setTime(time);
            kline.setTurnover(amount.multiply(BigDecimal.valueOf(VOLUME_PERCENT)));
            kline.setVolume(vol.multiply(BigDecimal.valueOf(VOLUME_PERCENT)));

            if(i==klineList.size()-1) {
                lastKline = new KLine(period);
                BeanUtil.copyProperties(kline,lastKline);
                high = highPokePrice == null ? high : highPokePrice.compareTo(high) == 1 ? highPokePrice : high; // Close price
                low = lowPokePrice == null ? low : lowPokePrice.compareTo(low) == -1 ? lowPokePrice : low; // Close price
                kline.setHighestPrice(high);
                kline.setLowestPrice(low);
            }




            // 
            marketService.saveKLine(symbol, kline);

            // Push K-line (if there is only one, it is the latest K-line and needs to be pushed to the frontend)
            if(klineList.size() == 1) {
                // 
//                    exchangePushJob.pushTickKline(symbol, kline);
                needPushList.add(kline);
            }else if(klineList.size() > 1){
                if(i == klineList.size() - 1) {
                    // 
//                        exchangePushJob.pushTickKline(symbol, kline);
                    needPushList.add(kline);
                }
            }
        }

        // Compensation K-line
        if(lastKline!=null){

            String period1 = lastKline.getPeriod();
            ContractCoinMatch match = matchFactory.getContractCoinMatch(symbol);
            BigDecimal lastPrice = match.getNowPrice();
            if(period1.endsWith("min")){
                Long end =  getMinTime(period1.replace("min",""),0);
                Long start =  getMinTime(period1.replace("min",""),-1);
                if(end > lastKline.getTime()){
                    KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKline, start,end);
                    // 
                    needPushList.add(newKLine);
//                        exchangePushJob.pushTickKline(symbol, newKLine);
                }
            }
            if(period1.endsWith("hour")){
                Long end =  getHourTime(period1.replace("hour",""),0);
                Long start =  getHourTime(period1.replace("hour",""),-1);
                if(end > lastKline.getTime()){
                    KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKline, start,end);
                    needPushList.add(newKLine);
//                        exchangePushJob.pushTickKline(symbol, newKLine);
                }
            }
            if(period1.endsWith("day")){
                Long end =  getHourTime(period1.replace("day",""),0);
                Long start =  getHourTime(period1.replace("day",""),-1);
                if(end > lastKline.getTime()){
                    KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKline, start,end);
//                        exchangePushJob.pushTickKline(symbol, newKLine);
                    needPushList.add(newKLine);
                }
            }
        }
        if(needPushList.size()>0){
            exchangePushJob.pushTickKline(symbol, needPushList);
        }

    }


    private KLine createNewTempLastKLine(String symbol, String period, BigDecimal lastPrice, KLine temKline, Long start,Long end) {
        // Get the highest and lowest prices of the 1-minute candles within this period
        KLine kline = new KLine(period);
        kline.setClosePrice(lastPrice);
        kline.setCount(temKline.getCount());
        kline.setHighestPrice( temKline.getClosePrice());
        kline.setLowestPrice(temKline.getClosePrice());
        kline.setOpenPrice(temKline.getClosePrice());
        kline.setTime(end);
        kline.setTurnover(temKline.getTurnover().multiply(BigDecimal.valueOf(VOLUME_PERCENT)));
        kline.setVolume(temKline.getVolume());
        return kline;
    }

    public static void main(String[] args) {
        Long end =  getMinTime("5min".replace("min",""),0);
        System.out.println(end);
    }

    private static Long getMinTime(String resolution,Integer count){
        int pr = Integer.parseInt(resolution);
        Calendar calendar = Calendar.getInstance();
        int m = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MINUTE,((m/pr)+count) * pr);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime().getTime()/1000;
    }

    private static Long getHourTime(String resolution,Integer count){
        int pr = Integer.parseInt(resolution);
        Calendar calendar = Calendar.getInstance();
        int m = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.set(Calendar.HOUR_OF_DAY,((m/pr)+count) * pr);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime().getTime()/1000;
    }

    private static Long getDayTime(String resolution,Integer count){
        int pr = Integer.parseInt(resolution);
        Calendar calendar = Calendar.getInstance();
        int m = calendar.get(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH,((m/pr)+count) * pr);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        System.out.println(calendar.getTime().getTime());
        return calendar.getTime().getTime()/1000;
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

class LimitedSizeTreeMap<K, V> extends TreeMap<K, V> {

    private static final int MAX_ENTRIES = 40;

    public LimitedSizeTreeMap() {
    }

    public LimitedSizeTreeMap(Comparator<? super K> comparator) {
        super(comparator);
    }

    @Override
    public V put(K key, V value) {
        // Remove zero values
        if(((BigDecimal)value).intValue() == 0 ){
            // If it exists, remove it
            return remove(key);
        }
        if (size() >= MAX_ENTRIES) {
            K lastKey = lastKey();
            remove(lastKey);
        }
        return super.put(key, value);
    }
}
