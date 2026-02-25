package com.wikex.wikex.kline.controller;

import com.wikex.wikex.kline.entity.Symbol;
import com.wikex.wikex.kline.service.KlineRobotMarketService;
import com.wikex.wikex.kline.util.DateUtil;
import com.wikex.wikex.kline.util.WebSocketConnectionManage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class MarketController {

    private Logger logger = LoggerFactory.getLogger(MarketController.class);
    @Autowired
    private KlineRobotMarketService klineRobotMarketService;

    //    public static String PERIOD[] ={ "1min", "5min", "15min", "30min", "60min","4hour", "1day", "1mon", "1week" };
    public static String PERIOD[] ={ "1min", "5min", "15min", "30min", "60min", "4hour", "1day", "1mon", "1week" };
//    public static String PERIOD[] ={ "1min","60min", "1day" };

    /**
     * Get supported trading pairs
     * @return
     */
    @RequestMapping("symbol/find")
    public List<Symbol> findAllSymbol(){
        List<Symbol> coins = klineRobotMarketService.findAllSymbol();
        return coins;
    }

    /**
     * Add supported trading pair
     * @return
     */
    @RequestMapping("symbol/add")
    public String addSymbol(@RequestParam("symbol")String symbol){
        Symbol symbol1 = new Symbol();
        symbol1.setSymbol(symbol);
        klineRobotMarketService.deleteAll(symbol);
        klineRobotMarketService.addSymbol(symbol1);
        return "success";
    }

    /**
     * Delete supported trading pair
     * @return
     */
    @RequestMapping("symbol/del")
    public String delSymbol(@RequestParam("symbol")String symbol){
        Symbol symbol1 = new Symbol();
        symbol1.setSymbol(symbol);
        klineRobotMarketService.deleteAll(symbol);
        return "success";
    }

    /**
     * Executed every 1 minute: Check and sync K-line
     */
    @RequestMapping("symbol/syncKLine")
    public void syncKLine(@RequestParam("symbol")String symbol){
//        List<Symbol> symbols = klineRobotMarketService.findAllSymbol();

        // Get current time (seconds)
        Long currentTime = DateUtil.getTimeMillis() / 1000;
        // Initialize K-line, time points
        // Binance API reference
        // symbol   STRING  YES
        // interval ENUM    YES   See enum definition: K-line intervals
        // startTime LONG   NO
        // endTime   LONG   NO
        // limit     INT    NO    Default 500; Maximum 1000
        // 1s
        // 1m
        // 3m
        // 5m
        // 15m
        // 30m
        // 1h
        // 2h
        // 4h
        // 6h
        // 8h
        // 12h
        // 1d
        // 3d
        // 1w
        // 1M
        // Example: https://api.binance.com/api/v3/klines?symbol=BNBUSDT&interval=1d
//        int count = 2000;
        logger.info("Execute every minute to get K-line [Start]");
        for(String period : PERIOD) {
            long fromTime = 0;
            // long fromTime = coinItem.getLastPeriodTime(period) + 1; // +1 to avoid fetching the last K-line again
            long timeGap = currentTime - fromTime;

            if(period.equals("1min") && timeGap >= 60 * 2) { // exceeded 1 minute
                if(fromTime == 0) {
                    // Initialize K-line, get the latest 600 K-lines
                    WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 60 * 2500, currentTime);
                }
            }

            if(period.equals("5min") && timeGap >= 60 * 5 * 2) { // exceeded 5 minutes
                if(fromTime == 0) {
                    // Initialize K-line, get the latest 600 K-lines
                    WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 5 * 60 * 1000, currentTime);
                }
            }

            if(period.equals("15min") && timeGap >= (60 * 15 * 2)) { // exceeded 15 minutes
                if(fromTime == 0) {
                    // Initialize K-line, get the latest 600 K-lines
                    WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 15 * 60 * 1000, currentTime);
                }
            }

            if(period.equals("30min") && timeGap >= (60 * 30 * 2)) { // exceeded 30 minutes
                if(fromTime == 0) {
                    // Initialize K-line, get the latest 600 K-lines
                    WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 30 * 60 * 1000, currentTime);
                }
            }

            if(period.equals("60min") && timeGap >= (60 * 60 * 2)) { // exceeded 60 minutes
                if(fromTime == 0) {
                    // Initialize K-line, get the latest 600 K-lines
                    WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 60 * 60 * 1000, currentTime);
                }
            }

            if(period.equals("4hour") && timeGap >= (60 * 60 * 4 * 2)) { // exceeded 4 hours
                if(fromTime == 0) {
                    // Initialize K-line, get the latest 600 K-lines
                    WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 4 * 60 * 60 * 600, currentTime);
                }
            }

            if(period.equals("1day") && timeGap >= (60 * 60 * 24 * 2)) { // exceeded 24 hours
                if(fromTime == 0) {
                    // Initialize K-line, get the latest 600 K-lines
                    WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 24 * 60 * 60 * 1000, currentTime);
                }
            }

            if(period.equals("1week") && timeGap >= (60 * 60 * 24 * 7 * 2)) { // exceeded 1 week
                if(fromTime == 0) {
                    // Initialize K-line, get the latest 600 K-lines
                    WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 7 * 24 * 60 * 60 * 500, currentTime);
                }
            }

            if(period.equals("1mon") && timeGap >= (60 * 60 * 24 * 30 * 2)) { // exceeded 1 month
                if(fromTime == 0) {
                    // Initialize K-line, get the latest 600 K-lines
                    WebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, currentTime - 30 * 24 * 60 * 60 * 100, currentTime);
                }
            }
        }
    }
}
