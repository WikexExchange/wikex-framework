package com.wikex.wikex.second.job;

import com.wikex.wikex.second.engine.ContractCoinMatchFactory;
import com.wikex.wikex.second.service.ContractMarketService;
import com.wikex.wikex.second.util.WebSocketConnectionManage;
import com.wikex.wikex.util.DateUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class KLineSyncJob {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(KLineSyncJob.class);

    @Autowired
    private ContractCoinMatchFactory matchFactory;

    @Autowired
    private ContractMarketService contractMarketService;

    public static String PERIOD[] ={ "1min", "5min", "15min", "30min", "60min","4hour", "1day", "1mon", "1week" };

    private List<CoinSyncItem> coinList = new ArrayList<CoinSyncItem>(); 

    

    @XxlJob("syncKLine")
    public void syncKLine(){

        
        if(coinList.size() == 0 || coinList.size() != matchFactory.getMatchMap().size()) this.initCoinList();

        
        Long currentTime = DateUtil.getTimeMillis() / 1000;

        logger.info("Per-minute K-line fetch [Start]");
        for(CoinSyncItem coinItem : coinList) {
            for(String period : PERIOD) {
                long fromTime = contractMarketService.findMaxTimestamp(coinItem.getSymbol(), period);
                
                long timeGap = currentTime - fromTime;
                if(period.equals("1min") && timeGap >= 60 * 2) { 
                    if(fromTime == 0) {
                        
                        
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        
                        long toTime = fromTime + (timeGap / 60) * 60 - 5;
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("5min") && timeGap >= 60 * 5 * 2) { 
                    if(fromTime == 0) {
                        
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 5 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / 300) * 300 - 5;
                        
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("15min") && timeGap >= (60 * 15 * 2)) { 
                    if(fromTime == 0) {
                        
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 15 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 15)) * 60 * 15 - 5;
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("30min") && timeGap >= (60 * 30 * 2)) { 
                    if(fromTime == 0) {
                        
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 30)) * 60 * 30 - 5;
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("60min") && timeGap >= (60 * 60 * 2)) { 
                    if(fromTime == 0) {
                        
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 60)) * 60 * 60 - 5;
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("4hour") && timeGap >= (60 * 60 * 4 * 2)) { 
                    if(fromTime == 0) {
                        
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 4 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 60 * 4)) * 60 * 60 * 4 - 5;
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("1day") && timeGap >= (60 * 60 * 24 * 2)) { 
                    if(fromTime == 0) {
                        
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 24 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 60 * 24)) * 60 * 60 * 24 - 5;
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("1week") && timeGap >= (60 * 60 * 24 * 7 * 2)) { 
                    if(fromTime == 0) {
                        
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 7 * 24 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 60 * 4 * 7)) * 60 * 60 * 4 * 7 - 5;
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("1mon") && timeGap >= (60 * 60 * 24 * 30 * 2)) { 
                    if(fromTime == 0) {
                        
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 24 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        
                        long toTime = fromTime + (timeGap / (60 * 60 * 4 * 30)) * 60 * 60 * 4 * 30 - 5;
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
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

                        if(period.equals("1min")) WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 600, currentTime);
                        if(period.equals("5min")) WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 5 * 60 * 600, currentTime);
                        if(period.equals("15min")) WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 15 * 60 * 600, currentTime);
                        if(period.equals("30min")) WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 60 * 600, currentTime);
                        if(period.equals("60min")) WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 60 * 600, currentTime);
                        if(period.equals("4hour")) WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 4 * 60 * 60 * 600, currentTime);
                        if(period.equals("1day")) WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 24 * 60 * 60 * 600, currentTime);
                        if(period.equals("1week")) WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 7 * 24 * 60 * 60 * 600, currentTime);
                        if(period.equals("1mon")) WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 24 * 60 * 60 * 600, currentTime);

                        coinItem.setLastPeriodTime(period, currentTime);
                    }
                }
                logger.info("Initializing K-line task coin list, adding coin: {}", symbol);
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
                    logger.info("Initializing K-line task coin list, adding coin: {}", symbol);
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
