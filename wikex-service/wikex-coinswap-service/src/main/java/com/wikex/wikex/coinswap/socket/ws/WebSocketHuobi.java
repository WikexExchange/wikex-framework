package com.wikex.wikex.coinswap.socket.ws;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.coinswap.engine.ContractCoinMatch;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.pojo.*;
import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.job.ExchangePushJob;
import com.wikex.wikex.coinswap.service.ContractMarketService;
import com.wikex.wikex.coinswap.util.ZipUtils;
import com.wikex.wikex.util.JSONUtils;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
public class WebSocketHuobi extends WebSocketClient {

    private ArrayList<String> subCoinList = new ArrayList<String>();

    private ContractCoinMatchFactory matchFactory;
    private ContractMarketService marketService;
    private ExchangePushJob exchangePushJob;

    private List<String> platformCoins;

    public static String DEPTH = "market.%s.depth.step0"; 
    public static String KLINE = "market.%s.kline.%s"; 
    public static String DETAIL = "market.%s.detail"; 
    public static String TRADE = "market.%s.trade.detail"; 

    private double VOLUME_PERCENT = 0.13; 

    public static String PERIOD[] = { "1min", "5min", "15min", "30min", "60min","4hour", "1day", "1mon", "1week" };

    public WebSocketHuobi(URI uri, ContractCoinMatchFactory matchFactory, ContractMarketService service, ExchangePushJob pushJob, List<String> platformCoins) {
        super(uri);
        this.uri = uri;
        this.matchFactory = matchFactory;
        this.marketService = service;
        this.exchangePushJob = pushJob;
        this.platformCoins=platformCoins;
    }

    @Override
    public void onOpen(ServerHandshake shake) {
        
        if (null != this.matchFactory.getMatchMap() && this.matchFactory.getMatchMap().size() > 0) {
            
            for(String symbol : this.matchFactory.getMatchMap().keySet()) {
                
                if(!platformCoins.contains(symbol.split("/")[0]) && !platformCoins.contains(symbol.split("/")[1])){
                    if(!subCoinList.contains(symbol)){
                        subCoinList.add(symbol);
                    }
                }
            }
            for(String symbol : subCoinList) {
                
                String depthTopic = String.format(DEPTH, symbol.replace("/", "").toLowerCase());
                
                sendWsMarket("sub", depthTopic);

                
                String detailTopic = String.format(DETAIL, symbol.replace("/", "").toLowerCase());
                
                sendWsMarket("sub", detailTopic);

                
                String tradeTopic = String.format(TRADE, symbol.replace("/", "").toLowerCase());
                
                sendWsMarket("sub", tradeTopic);

                





            }
        }
    }

    
    public void subNewCoin(String symbol) {
        if(!subCoinList.contains(symbol)){
            subCoinList.add(symbol);

            String detailTopic = String.format(DETAIL, symbol.replace("/", "").toLowerCase());
            
            sendWsMarket("sub", detailTopic);

            String tradeTopic = String.format(TRADE, symbol.replace("/", "").toLowerCase());
            
            sendWsMarket("sub", tradeTopic);

            String depthTopic = String.format(DEPTH, symbol.replace("/", "").toLowerCase());
            
            sendWsMarket("sub", depthTopic);
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
            
        }
    }

    @Override
    public void onError(Exception arg0) {
        log.error("[WebSocketHuobi] has error ,the message is :: {}", arg0.getMessage());
        arg0.printStackTrace();
        String message = "";
        try {
            message = new String(arg0.getMessage().getBytes(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[WebSocketHuobi] has error ,the message is :: {}", message);
        }
    }

    @Override
    public void onClose(int arg0, String arg1, boolean arg2) {
        
        int tryTimes = 0;
        
        
        if(this.getReadyState().equals(ReadyState.NOT_YET_CONNECTED) || this.getReadyState().equals(ReadyState.CLOSED) || this.getReadyState().equals(ReadyState.CLOSING)) {

            Runnable sendable = new Runnable() {
                @Override
                public void run() {
                    
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
                                            lowPokePrice = price;
                                        }
                                    }

                                    if(highPokePrice == null){
                                        highPokePrice = price;
                                    }else {
                                        if(highPokePrice.compareTo(price)==-1){
                                            highPokePrice = price;
                                        }
                                    }
                                }

                            }

                            JSONArray klineList = jsonObject.getJSONArray("data");
                            KLine lastKline = null;
                            for(int i = 0; i < klineList.size(); i++) {
                                JSONObject klineObj = klineList.getJSONObject(i);

                                BigDecimal open = klineObj.getBigDecimal("open") ; 
                                BigDecimal close = klineObj.getBigDecimal("close") ;
                                BigDecimal high =  klineObj.getBigDecimal("high");
                                BigDecimal low =  klineObj.getBigDecimal("low");
                                if(i==klineList.size()-1) {
                                    high = highPokePrice == null ? klineObj.getBigDecimal("high") : highPokePrice.compareTo(klineObj.getBigDecimal("high")) == 1 ? highPokePrice : klineObj.getBigDecimal("high"); 
                                    low = lowPokePrice == null ? klineObj.getBigDecimal("low") : lowPokePrice.compareTo(klineObj.getBigDecimal("low")) == -1 ? lowPokePrice : klineObj.getBigDecimal("low"); 
                                }
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
                                lastKline = kline;
                                
                                if(klineList.size() == 1) {
                                    
                                    exchangePushJob.pushTickKline(symbol, kline);
                                }else if(klineList.size() > 1){
                                    if(i == klineList.size() - 1) {
                                        
                                        exchangePushJob.pushTickKline(symbol, kline);
                                    }
                                }
                            }
                            
                            if(lastKline!=null){
                                String period1 = lastKline.getPeriod();
                                ContractCoinMatch match = matchFactory.getContractCoinMatch(symbol);
                                BigDecimal lastPrice = match.getNowPrice();
                                if(period1.endsWith("min")){
                                    Long end =  getMinTime(period1.replace("min",""),0);
                                    Long start =  getMinTime(period1.replace("min",""),-1);
                                    if(end > lastKline.getTime()){
                                        KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKline, start,end);
                                        exchangePushJob.pushTickKline(symbol, newKLine);
                                    }
                                }
                                if(period1.endsWith("hour")){
                                    Long end =  getHourTime(period1.replace("hour",""),0);
                                    Long start =  getHourTime(period1.replace("hour",""),-1);
                                    if(end > lastKline.getTime()){
                                        KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKline, start,end);
                                        exchangePushJob.pushTickKline(symbol, newKLine);
                                    }
                                }
                                if(period1.endsWith("day")){
                                    Long end =  getHourTime(period1.replace("day",""),0);
                                    Long start =  getHourTime(period1.replace("day",""),-1);
                                    if(end > lastKline.getTime()){
                                        KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKline, start,end);
                                        exchangePushJob.pushTickKline(symbol, newKLine);
                                    }
                                }
                            }

                        }
                    }else if(type.equals("depth")){
                        String tick = jsonObject.getString("tick");
                        if (null != tick && !"".equals(tick) && JSONUtils.isJsonObject(tick)) {

                            JSONObject plateObj = JSONObject.parseObject(tick);

                            
                            JSONArray bids = plateObj.getJSONArray("bids");
                            List<TradePlateItem> buyItems = new ArrayList<>();
                            for(int i = 0; i < bids.size(); i++) {
                                TradePlateItem item = new TradePlateItem();
                                JSONArray itemObj = bids.getJSONArray(i);
                                item.setPrice(itemObj.getBigDecimal(0));
                                item.setAmount(itemObj.getBigDecimal(1));
                                buyItems.add(item);
                            }

                            
                            JSONArray asks = plateObj.getJSONArray("asks");
                            List<TradePlateItem> sellItems = new ArrayList<>();
                            for(int i = 0; i < asks.size(); i++) {
                                TradePlateItem item = new TradePlateItem();
                                JSONArray itemObj = asks.getJSONArray(i);
                                item.setPrice(itemObj.getBigDecimal(0));
                                item.setAmount(itemObj.getBigDecimal(1));
                                sellItems.add(item);
                            }
                            
                            this.matchFactory.getContractCoinMatch(symbol).refreshPlate(buyItems, sellItems);

                            
                        }
                    }else if(type.equals("detail")){ 
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
                                            lowPokePrice = price;
                                        }
                                    }

                                    if(highPokePrice == null && detailObj.getBigDecimal("high").compareTo(price)==-1){
                                        highPokePrice = price;
                                    }else if(highPokePrice!=null){
                                        if(highPokePrice.compareTo(price)==-1){
                                            highPokePrice = price;
                                        }
                                    }
                                }
                            }

                            BigDecimal amount = detailObj.getBigDecimal("amount");
                            BigDecimal open = detailObj.getBigDecimal("open") ;
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
                            thumb.setVolume(amount.multiply(BigDecimal.valueOf(VOLUME_PERCENT))); 
                            thumb.setTurnover(vol.multiply(BigDecimal.valueOf(VOLUME_PERCENT))); 
                            this.matchFactory.getContractCoinMatch(symbol).refreshThumb(thumb);
                            if(lowPokePrice!=null && highPokePrice!=null && lowPokePrice.compareTo(highPokePrice)==0){
                                highPokePrice = null;
                            }
                            

                            
                            if(lowPokePrice == null && highPokePrice == null){
                                this.matchFactory.getContractCoinMatch(symbol).refreshPrice(close);
                            }else {
                                if(lowPokePrice!=null){
                                    this.matchFactory.getContractCoinMatch(symbol).refreshPrice(lowPokePrice);
                                    
                                }
                                if(highPokePrice!=null){
                                    this.matchFactory.getContractCoinMatch(symbol).refreshPrice(highPokePrice);
                                    
                                }
                            }
                            
                        }
                    }else if(type.equals("trade")) { 
                        String tick = jsonObject.getString("tick");
                        if (null != tick && !"".equals(tick) && JSONUtils.isJsonObject(tick)) {
                            List<Poke> pokes = marketService.findPokeAndRemove(symbol,type,null);

                            JSONObject detailObj = JSONObject.parseObject(tick);
                            JSONArray tradeList = detailObj.getJSONArray("data");
                            List<ContractTrade> tradeArrayList = new ArrayList<ContractTrade>();
                            for(int i = 0; i < tradeList.size(); i++) {
                                BigDecimal amount = tradeList.getJSONObject(i).getBigDecimal("amount");
                                BigDecimal price =  tradeList.getJSONObject(i).getBigDecimal("price");
                                String direction = tradeList.getJSONObject(i).getString("direction");
                                long time = tradeList.getJSONObject(i).getLongValue("ts");
                                String tradeId = tradeList.getJSONObject(i).getString("tradeId");

                                
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
                            
                            this.matchFactory.getContractCoinMatch(symbol).refreshLastedTrade(tradeArrayList);
                            
                        }
                    }
                }
            }
        } catch (CharacterCodingException e) {
            e.printStackTrace();
            log.error("[WebSocketHuobi] websocket exception: {}", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[WebSocketHuobi] websocket exception: {}", e.getMessage());
        }
    }

    private KLine createNewTempLastKLine(String symbol, String period, BigDecimal lastPrice, KLine temKline, Long start,Long end) {
        List<Poke> pokes = marketService.findPoke(symbol, "kline", period);
        BigDecimal lowPokePrice = marketService.findMinPrice(symbol,"1min",start,end);
        BigDecimal highPokePrice = marketService.findMaxPrice(symbol,"1min",start,end);
        if (pokes != null && pokes.size() > 0) {
            for (Poke poke : pokes) {
                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(poke.getPrice()));
                if (lowPokePrice == null) {
                    lowPokePrice = price;
                } else {
                    if (lowPokePrice.compareTo(price) == 1) {
                        lowPokePrice = price;
                    }
                }

                if (highPokePrice == null) {
                    highPokePrice = price;
                } else {
                    if (highPokePrice.compareTo(price) == -1) {
                        highPokePrice = price;
                    }
                }
            }
        }
        
        KLine kline = new KLine(period);
        kline.setClosePrice(lastPrice);
        kline.setCount(temKline.getCount());
        kline.setHighestPrice(highPokePrice == null ? temKline.getClosePrice() : highPokePrice);
        kline.setLowestPrice(lowPokePrice == null ? temKline.getClosePrice() : lowPokePrice);
        kline.setOpenPrice(temKline.getClosePrice());
        kline.setTime(end);
        kline.setTurnover(temKline.getTurnover().multiply(BigDecimal.valueOf(VOLUME_PERCENT)));
        kline.setVolume(temKline.getVolume());
        return kline;
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
        return calendar.getTime().getTime()/1000;
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
