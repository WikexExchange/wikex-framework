package com.wikex.wikex.option.job;

import com.wikex.wikex.option.engine.ContractOptionCoinMatchFactory;
import com.wikex.wikex.option.service.ContractMarketService;
import com.wikex.wikex.option.util.WebSocketConnectionManage;
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
    private ContractOptionCoinMatchFactory matchFactory;

    @Autowired
    private ContractMarketService contractMarketService;

    public static String PERIOD[] ={ "1min", "5min", "15min", "30min", "60min","4hour", "1day", "1mon", "1week" };

    private List<CoinSyncItem> coinList = new ArrayList<CoinSyncItem>(); 

    /**
     * Execute once per minute: check
     */
    public void syncKLine(){

        // If the coin list size is 0, or the number of coins in the engine is inconsistent with the current list size
        if(coinList.size() == 0 || coinList.size() != matchFactory.getMatchMap().size()) this.initCoinList();

        // Get current time (seconds)
        Long currentTime = DateUtil.getTimeMillis() / 1000;

        logger.info("Execute every minute to fetch KLine [Start]");
        for(CoinSyncItem coinItem : coinList) {
            for(String period : PERIOD) {
                long fromTime = contractMarketService.findMaxTimestamp(coinItem.getSymbol(), period);
                
                long timeGap = currentTime - fromTime;
                if(period.equals("1min") && timeGap >= 60) { 
                    if(fromTime == 0) {
                        logger.info("Execute every minute to fetch KLine [1min] ===> from == 0");
                        // Initialization: fetch the latest 600 KLines
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        logger.info("Execute every minute to fetch KLine [1min] ===> from != 0, timeGap: {}", timeGap);
                        // Not initialization: fetch the recently generated KLines
                        long toTime = fromTime + timeGap - (timeGap % 60); 
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("5min") && timeGap >= 60 * 5) { 
                    if(fromTime == 0) {
                        // Initialization: fetch the latest 600 KLines
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 5 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        // Not initialization: fetch the recently generated KLines
                        long toTime = fromTime + timeGap - (timeGap % (5 * 60));
                        logger.info("Fetch 5-minute KLine, From: {}, To: {}", fromTime, toTime);
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("15min") && timeGap >= (60 * 15 + 60)) { 
                    if(fromTime == 0) {
                        // Initialization: fetch the latest 600 KLines
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 15 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        // Not initialization: fetch the recently generated KLines
                        long toTime = fromTime + timeGap - (timeGap % (15 * 60));
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("30min") && timeGap >= (60 * 30 + 60)) { 
                    if(fromTime == 0) {
                        // Initialization: fetch the latest 600 KLines
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        // Not initialization: fetch the recently generated KLines
                        long toTime = fromTime + timeGap - (timeGap % (30 * 60));
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("60min") && timeGap >= (60 * 60 + 60)) { 
                    if(fromTime == 0) {
                        // Initialization: fetch the latest 600 KLines
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        // Not initialization: fetch the recently generated KLines
                        long toTime = fromTime + timeGap - (timeGap % (60 * 60));
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("4hour") && timeGap >= (60 * 60 * 4 + 60)) { 
                    if(fromTime == 0) {
                        // Initialization: fetch the latest 600 KLines
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 4 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        // Not initialization: fetch the recently generated KLines
                        long toTime = fromTime + timeGap - (timeGap % (4 * 60 * 60));
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("1day") && timeGap >= (60 * 60 * 24 + 60)) { 
                    if(fromTime == 0) {
                        // Initialization: fetch the latest 600 KLines
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 24 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        // Not initialization: fetch the recently generated KLines
                        long toTime = fromTime + timeGap - (timeGap % (24 * 60 * 60));
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("1week") && timeGap >= (60 * 60 * 24 * 7 + 60)) { 
                    if(fromTime == 0) {
                        // Initialization: fetch the latest 600 KLines
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 7 * 24 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        // Not initialization: fetch the recently generated KLines
                        long toTime = fromTime + timeGap - (timeGap % (7 * 24 * 60 * 60));
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime - 10);
                    }
                }

                if(period.equals("1mon") && timeGap >= (60 * 60 * 24 * 30 + 60)) { 
                    if(fromTime == 0) {
                        // Initialization: fetch the latest 600 KLines
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 24 * 60 * 60 * 600, currentTime);
                        coinItem.setLastPeriodTime(period, currentTime);
                    }else{
                        // Not initialization: fetch the recently generated KLines
                        long toTime = fromTime + timeGap - (timeGap % (30 * 24 * 60 * 60));
                        WebSocketConnectionManage.getWebSocket().reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                        coinItem.setLastPeriodTime(period, toTime);
                    }
                }
            }
        }
    }

    /**
     * Execute once every 5 minutes: check if there are new trading pairs
     */
    public void checkNewCoin(){

    }

    /**
     * Execute once every hour: synchronize the latest KLine update time of the coin
     * This is mainly to prevent issues with syncKLine job
     */
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
        // No list (initialization)
        if(coinList.size() == 0) {
            for(String symbol : this.matchFactory.getMatchMap().keySet()) {
                CoinSyncItem coinItem = new CoinSyncItem(PERIOD);
                coinItem.setSymbol(symbol);

                // Set last time for each KLine period
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
                logger.info("Initialize KLine task coin list, add coin: {}", symbol);
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

                    // Set last time for each KLine period
                    for(String p : PERIOD){
                        long lastTime = contractMarketService.findMaxTimestamp(symbol, p);
                        cs.setLastPeriodTime(p, lastTime);
                    }
                    logger.info("Initialize KLine task coin list, add coin: {}", symbol);
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
