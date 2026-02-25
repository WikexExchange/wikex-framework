package com.wikex.wikex.coinswap.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.coinswap.engine.ContractCoinMatch;
import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.service.ContractMarketService;
import com.wikex.wikex.coinswap.util.WikexWebSocketConnectionManage;
import com.wikex.wikex.coinswap.util.HuobiWebSocketConnectionManage;
import com.wikex.wikex.market.feign.MarketFeign;
import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.Poke;
import com.wikex.wikex.util.DateUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KLineSyncJob {

    @Autowired
    private ContractCoinMatchFactory matchFactory;
    @Autowired
    private ContractMarketService marketService;
    @Autowired
    private MarketFeign marketFeign;
    @Autowired
    private ExchangePushJob exchangePushJob;

    @Autowired
    private ContractMarketService contractMarketService;

    public static String PERIOD[] ={ "1min", "5min", "15min", "30min", "60min","4hour", "1day", "1mon", "1week" };

    private List<CoinSyncItem> coinList = new ArrayList<CoinSyncItem>(); 

    private double VOLUME_PERCENT = 1; 

    @Value("${platformCoins}")
    private String platformCoins;

    

    @XxlJob("syncKLine")
    public void syncKLine(){

        
        if(coinList.size() == 0 || coinList.size() != matchFactory.getMatchMap().size()) this.initCoinList();

        
        Long currentTime = DateUtil.getTimeMillis() / 1000;

        
        for(CoinSyncItem coinItem : coinList) {
            for(String period : PERIOD) {
                long fromTime = contractMarketService.findMaxTimestamp(coinItem.getSymbol(), period);
                
                long timeGap = currentTime - fromTime;
                if(period.equals("1min") && timeGap >= 60 * 2) { 
                    if(fromTime == 0) {
                        
                        
                        this.reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        
                        long toTime = fromTime + (timeGap / 60) * 60 - 5;
                        this.reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("5min") && timeGap >= 60 * (5 + 2)) { 
                    if(fromTime == 0) {
                        
                        this.reqKLineList(coinItem.getSymbol(), period, currentTime - 5 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / 300) * 300 - 5;
                        
                        this.reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("15min") && timeGap >= (60 * (15 + 2))) { 
                    if(fromTime == 0) {
                        
                        this.reqKLineList(coinItem.getSymbol(), period, currentTime - 15 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 15)) * 60 * 15 - 5;
                        this.reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("30min") && timeGap >= (60 * (30 + 2))) { 
                    if(fromTime == 0) {
                        
                        this.reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 30)) * 60 * 30 - 5;
                        this.reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("60min") && timeGap >= (60 * (60 + 2))) { 
                    if(fromTime == 0) {
                        
                        this.reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 60)) * 60 * 60 - 5;
                        this.reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("4hour") && timeGap >= (60 * 60 * (4 + 2))) { 
                    if(fromTime == 0) {
                        
                        this.reqKLineList(coinItem.getSymbol(), period, currentTime - 4 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 60 * 4)) * 60 * 60 * 4 - 5;
                        this.reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("1day") && timeGap >= (60 * 60 * (24 + 2))) { 
                    if(fromTime == 0) {
                        
                        this.reqKLineList(coinItem.getSymbol(), period, currentTime - 24 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 60 * 24)) * 60 * 60 * 24 - 5;
                        this.reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("1week") && timeGap >= (60 * 60 * 24 * (7 + 2))) { 
                    if(fromTime == 0) {
                        
                        this.reqKLineList(coinItem.getSymbol(), period, currentTime - 7 * 24 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 60 * 4 * 7)) * 60 * 60 * 4 * 7 - 5;
                        this.reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("1mon") && timeGap >= (60 * 60 * 24 * (30 + 2))) { 
                    if(fromTime == 0) {
                        
                        this.reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 24 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 60 * 4 * 30)) * 60 * 60 * 4 * 30 - 5;
                        this.reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime);
                    }
                }
            }
        }
    }

    

    public void checkNewCoin(){

    }

    

    @XxlJob("syncKLineTime")
    public void syncKLineTime() {
        for (CoinSyncItem coinItem : coinList) {
            for (String period : PERIOD) {
                long lastTime = contractMarketService.findMaxTimestamp(coinItem.getSymbol(), period);
                coinItem.setLastPeriodTime(period, lastTime);
            }
        }
    }

    private void reqKLineList(String symbol, String period, long from, long to) {
        
        List<String> platformCoinList =  Arrays.stream(platformCoins.split(",")).collect(Collectors.toList());
        if(platformCoinList.contains(symbol.split("/")[0]) || platformCoinList.contains(symbol.split("/")[1])){
            saveKLine4Platform(symbol, period, from, to);
        }else {
            HuobiWebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, from, to);
        }

    }

    private void saveKLine4Platform(String symbol,String period,Long from, Long to){
        List<Poke> pokes = marketService.findPokeAndRemove(symbol,"kline",period);
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
        List<KLine> allKLine = marketService.findAllKLine(symbol, from, to, period);
        List<Long> times = allKLine.stream().map(x -> x.getTime()).collect(Collectors.toList());
        from = from*1000;
        to = to*1000;
        String period4Feign = period;
        if("60min".equals(period)){
            period4Feign = "1hour";
        }
        List<KLine> klineList = marketFeign.findKHistory4Feign(symbol,from,to,period4Feign);
        KLine lastKline = null;
        for(int i = 0; i < klineList.size(); i++) {
            KLine klineObj = klineList.get(i);
            long time = klineObj.getTime()/1000;
            if(times.contains(time)){
                
                continue;
            }
            BigDecimal open = klineObj.getOpenPrice() ; 
            BigDecimal close = klineObj.getClosePrice() ;
            BigDecimal high =  klineObj.getHighestPrice();
            BigDecimal low =  klineObj.getLowestPrice();
            if(i==klineList.size()-1) {
                high = highPokePrice == null ? klineObj.getHighestPrice() : highPokePrice.compareTo(klineObj.getHighestPrice()) == 1 ? highPokePrice : klineObj.getHighestPrice(); 
                low = lowPokePrice == null ? klineObj.getLowestPrice() : lowPokePrice.compareTo(klineObj.getLowestPrice()) == -1 ? lowPokePrice : klineObj.getLowestPrice(); 
            }

            KLine kline = new KLine(period);
            kline.setClosePrice(close);
            kline.setCount(klineObj.getCount());
            kline.setHighestPrice(high);
            kline.setLowestPrice(low);
            kline.setOpenPrice(open);
            kline.setTime(time);
            kline.setTurnover(klineObj.getTurnover());
            kline.setVolume(klineObj.getVolume());
            marketService.saveKLine(symbol, kline);

            
            if(klineList.size() == 1) {
                lastKline = kline;
                
                exchangePushJob.pushTickKline(symbol, kline);
            }else if(klineList.size() > 1){
                if(i == klineList.size() - 1) {
                    lastKline = kline;
                    
                    exchangePushJob.pushTickKline(symbol, kline);
                }
            }
        }

    }

    private void initCoinList(){
        
        if(coinList.size() == 0) {
            for(String symbol : this.matchFactory.getMatchMap().keySet()) {
                CoinSyncItem coinItem = new CoinSyncItem(PERIOD);
                coinItem.setSymbol(symbol);

                
                for(String period : PERIOD){
                    long lastTime = contractMarketService.findMaxTimestamp(symbol, period);
                    coinItem.setLastPeriodTime(period, lastTime);

                    if(lastTime == 0) {
                        Long currentTime = DateUtil.getTimeMillis() / 1000;

                        if(period.equals("1min")) this.reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 600, currentTime);
                        if(period.equals("5min")) this.reqKLineList(coinItem.getSymbol(), period, currentTime - 5 * 60 * 600, currentTime);
                        if(period.equals("15min")) this.reqKLineList(coinItem.getSymbol(), period, currentTime - 15 * 60 * 600, currentTime);
                        if(period.equals("30min")) this.reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 60 * 600, currentTime);
                        if(period.equals("60min")) this.reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 60 * 600, currentTime);
                        if(period.equals("4hour")) this.reqKLineList(coinItem.getSymbol(), period, currentTime - 4 * 60 * 60 * 600, currentTime);
                        if(period.equals("1day")) this.reqKLineList(coinItem.getSymbol(), period, currentTime - 24 * 60 * 60 * 600, currentTime);
                        if(period.equals("1week")) this.reqKLineList(coinItem.getSymbol(), period, currentTime - 7 * 24 * 60 * 60 * 600, currentTime);
                        if(period.equals("1mon")) this.reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 24 * 60 * 60 * 600, currentTime);

                        coinItem.setLastPeriodTime(period, currentTime);
                    }
                }
                
                coinList.add(coinItem);
            }
        }else{
            for(String symbol : this.matchFactory.getMatchMap().keySet()) {
                boolean hasValue = false;
                for(CoinSyncItem item : coinList) {
                    if(item.getSymbol().equals(symbol)) {
                        hasValue = true;
                    }
                }
                if(!hasValue) {
                    CoinSyncItem cs = new CoinSyncItem(PERIOD);
                    cs.setSymbol(symbol);

                    
                    for(String p : PERIOD){
                        long lastTime = contractMarketService.findMaxTimestamp(symbol, p);
                        cs.setLastPeriodTime(p, lastTime);
                    }
                    
                    coinList.add(cs);
                }
            }
        }
    }

    class CoinSyncItem{
        String symbol;
        Map<String, Long> lastUpdateTime;

        CoinSyncItem(String[] period){
            lastUpdateTime = new HashMap<String, Long>();
            for(String p : period) {
                lastUpdateTime.put(p, -1L);
            }
        }

        public String getSymbol() {
            return symbol;
        }
        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public void setLastPeriodTime(String period, Long time) {
            this.lastUpdateTime.put(period, time);
        }

        public Long getLastPeriodTime(String period) {
            return this.lastUpdateTime.get(period);
        }

    }
}
