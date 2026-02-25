package com.wikex.wikex.swap.socket.ws;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.pojo.*;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.job.ExchangePushJob;
import com.wikex.wikex.swap.service.ContractMarketService;
import com.wikex.wikex.swap.util.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;


import java.math.BigDecimal;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WebSocketwikex extends WebSocketClient{

    private ArrayList<String> subCoinList = new ArrayList<String>();

    private ContractCoinMatchFactory matchFactory;
    private ContractMarketService marketService;
    private ExchangePushJob exchangePushJob;

    private List<String> platformCoins;

    public static String TOPIC = "[\"SUBSCRIBE\\nid:sub-2\\ndestination:%s\\n\\n\\u0000\"]";

    public static String DEPTH = "/topic/market/trade-plate/%s"; 
    public static String KLINE = "/topic/market/kline/%s"; 
    public static String DETAIL = "/topic/market/thumb"; 
    public static String TRADE = "/topic/market/trade/%s"; 

    private double VOLUME_PERCENT = 1; 

    public static String PERIOD[] = { "1min", "5min", "15min", "30min", "60min","4hour", "1day", "1mon", "1week" };

    public WebSocketwikex(URI uri, ContractCoinMatchFactory matchFactory, ContractMarketService service, ExchangePushJob pushJob, List<String> platformCoins) {
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
                
                if(platformCoins.contains(symbol.split("/")[0]) || platformCoins.contains(symbol.split("/")[1])){
                    if(!subCoinList.contains(symbol)){
                        subCoinList.add(symbol);
                    }
                }
            }
            if(subCoinList.size()>0){
                
                
                sendWsMarket(DETAIL);
                for(String symbol : subCoinList) {
                    
                    String depthTopic = String.format(DEPTH, symbol);
                    
                    sendWsMarket(depthTopic);

                    
                    String tradeTopic = String.format(TRADE,symbol);
                    
                    sendWsMarket(tradeTopic);
                }
            }
        }
    }

    
    public void subNewCoin(String symbol) {
        if(!subCoinList.contains(symbol)){
            subCoinList.add(symbol);
            
            String tradeTopic = String.format(TRADE,symbol);
            
            sendWsMarket(tradeTopic);

            String depthTopic = String.format(DETAIL, symbol);
            
            sendWsMarket(depthTopic);
        }
    }

    
    public void reqKLineList(String symbol, String period, long from, long to) {
        
    }

    @Override
    public void onMessage(String arg0) {
        if (arg0 != null) {
            if(arg0.startsWith("a[\"MESSAGE\\ndestination:/topic/market/thumb\\ncontent-type:application/json;charset=UTF-8\\nsubscription:sub-2\\nmessage-id:")){
                String[] splitStrs = arg0.split("\\\\n\\\\n");
                if(splitStrs.length==2){
                    String json = splitStrs[1].replace("\\u0000\"]", "").replaceAll("\\\\","");
                    CoinThumb thumb = JSON.parseObject(json,CoinThumb.class);
                    String symbol = thumb.getSymbol();
                    if(subCoinList.contains(symbol)){
                        List<Poke> pokes = marketService.findPokeAndRemove(symbol,"detail",null);
                        BigDecimal lowPokePrice = null;
                        BigDecimal highPokePrice = null;
                        if(pokes!=null && pokes.size()>0){
                            for (Poke poke : pokes) {
                                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(poke.getPrice()));
                                if(lowPokePrice == null){
                                    lowPokePrice = price;
                                }else if(lowPokePrice!=null){
                                    if(lowPokePrice.compareTo(price)==1){
                                        lowPokePrice = price;
                                    }
                                }

                                if(highPokePrice == null){
                                    highPokePrice = price;
                                }else if(highPokePrice!=null){
                                    if(highPokePrice.compareTo(price)==-1){
                                        highPokePrice = price;
                                    }
                                }
                            }
                        }

                        BigDecimal high = highPokePrice==null ? thumb.getHigh() : highPokePrice.compareTo(thumb.getHigh())==1?highPokePrice:thumb.getHigh();
                        BigDecimal low = lowPokePrice==null ? thumb.getLow() : lowPokePrice.compareTo(thumb.getLow())==-1?lowPokePrice:thumb.getLow();
                        thumb.setHigh(high);
                        thumb.setLow(low);
                        this.matchFactory.getContractCoinMatch(symbol).refreshThumb(thumb);
                        if(lowPokePrice!=null && highPokePrice!=null && lowPokePrice.compareTo(highPokePrice)==0){
                            highPokePrice = null;
                        }
                        
                        
                        if(lowPokePrice == null && highPokePrice == null){
                            this.matchFactory.refreshPrice(symbol,thumb.getClose());
                        }else {
                            if(lowPokePrice!=null){
                                this.matchFactory.refreshPrice(symbol,lowPokePrice);
                                
                            }
                            if(highPokePrice!=null){
                                this.matchFactory.refreshPrice(symbol,highPokePrice);
                                
                            }
                        }
                    }
                }
            }else if(arg0.startsWith("a[\"MESSAGE\\ndestination:/topic/market/trade/")){
                for (String symbol : subCoinList) {
                    if(arg0.startsWith("a[\"MESSAGE\\ndestination:/topic/market/trade/"+symbol)){
                        String[] splitStrs = arg0.split("\\\\n\\\\n");
                        if(splitStrs.length==2){
                            String json = splitStrs[1].replace("\\u0000\"]", "").replaceAll("\\\\","");
                            List<ExchangeTrade> trades = JSON.parseArray(json,ExchangeTrade.class);
                            List<ContractTrade> tradeArrayList = new ArrayList<ContractTrade>();
                            if(trades!=null && trades.size()>0){
                                List<Poke> pokes = marketService.findPokeAndRemove(symbol,"trade",null);
                                for (ExchangeTrade trade1 : trades) {
                                    BigDecimal amount = trade1.getAmount();
                                    BigDecimal price =  trade1.getPrice();
                                    int direction = trade1.getDirection().getCode();
                                    long time = trade1.getTime();
                                    
                                    ContractTrade trade = new ContractTrade();
                                    trade.setAmount(amount);
                                    trade.setPrice(price);
                                    if(direction == 0) {
                                        trade.setDirection(ContractOrderDirection.BUY);
                                        trade.setBuyOrderId(trade1.getBuyOrderId());
                                        trade.setBuyTurnover(amount.multiply(price));
                                    }else{
                                        trade.setDirection(ContractOrderDirection.SELL);
                                        trade.setSellOrderId(trade1.getSellOrderId());
                                        trade.setSellTurnover(amount.multiply(price));
                                    }
                                    trade.setSymbol(symbol);
                                    trade.setTime(time);
                                    tradeArrayList.add(trade);
                                }

                                if(pokes!=null && pokes.size()>0){
                                    for (int i = 0; i < pokes.size(); i++) {
                                        int index = i;
                                        if(i>=trades.size()-1){
                                            index = trades.size()-1;
                                        }
                                        BigDecimal amount = trades.get(index).getAmount();
                                        int direction = trades.get(index).getDirection().getCode();
                                        long time = trades.get(index).getTime();
                                        BigDecimal price = BigDecimal.valueOf(Double.parseDouble(pokes.get(i).getPrice()));
                                        
                                        ContractTrade trade = new ContractTrade();
                                        trade.setAmount(amount);
                                        trade.setPrice(price);
                                        if(direction == 0 ) {
                                            trade.setDirection(ContractOrderDirection.BUY);
                                            trade.setBuyOrderId(trades.get(index).getBuyOrderId()+i);
                                            trade.setBuyTurnover(amount.multiply(price));
                                        }else{
                                            trade.setDirection(ContractOrderDirection.SELL);
                                            trade.setSellOrderId(trades.get(index).getSellOrderId()+i);
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
            }else if(arg0.startsWith("a[\"MESSAGE\\ndestination:/topic/market/trade-plate")){
                for (String symbol : subCoinList) {
                    if(arg0.startsWith("a[\"MESSAGE\\ndestination:/topic/market/trade-plate/"+symbol)){
                        String[] splitStrs = arg0.split("\\\\n\\\\n");
                        if(splitStrs.length==2){
                            String json = splitStrs[1].replace("\\u0000\"]", "").replaceAll("\\\\","");
                            TradePlate tradePlate = JSON.parseObject(json,TradePlate.class);
                            List<TradePlateItem> buyItems = new ArrayList<>();
                            List<TradePlateItem> sellItems = new ArrayList<>();
                            if(tradePlate.getDirection().getCode()==0){
                                buyItems = tradePlate.getItems();
                            }else{
                                sellItems = tradePlate.getItems();
                            }
                            if(sellItems.size()>0 || buyItems.size()>0){
                                
                                this.matchFactory.getContractCoinMatch(symbol).refreshPlate(buyItems, sellItems);
                            }
                        }
                    }
                }
            }

        }
    }

    @Override
    public void onError(Exception arg0) {
        log.error("[WebSocketwikex] has error ,the message is :: {}", arg0.getMessage());
        // arg0.printStackTrace();
        String message = "";
        try {
            message = new String(arg0.getMessage().getBytes(), "UTF-8");
        } catch (Exception e) {
            // e.printStackTrace();
            log.error("[WebSocketwikex] has error ,the message is :: {}", message);
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
            // log.error("[WebSocketwikex] message: {}", message);
            if (message != null && !message.isEmpty()) {
                onMessage(message);
            }
        } catch (CharacterCodingException e) {
            // e.printStackTrace();
            log.error("[WebSocketwikex] websocket exception: {}", e.getMessage());
        } catch (Exception e) {
            // e.printStackTrace();
            log.error("[WebSocketwikex] websocket exception: {}", e.getMessage());
        }
    }

    public void sendWsMarket(String topic) {
        String format = String.format(TOPIC, topic);
        send(format);
    }

    public void sendWsMarket(String op, String topic, long from, long to) {
        JSONObject req = new JSONObject();
        req.put(op, topic);
        req.put("from", from);
        req.put("to", to);
        send(req.toString());
    }
}
