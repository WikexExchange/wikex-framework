package com.wikex.wikex.swap.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.market.feign.MarketFeign;
import com.wikex.wikex.pojo.ContractTrade;
import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.Poke;
import com.wikex.wikex.pojo.TradePlateItem;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.ContractMarketService;
import com.wikex.wikex.swap.util.HuobiWebSocketConnectionManage;
import com.wikex.wikex.swap.util.IFindUtils;
import com.wikex.wikex.user.entity.Currency;
import com.wikex.wikex.util.DateUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class KLineSyncJob {

    @Autowired
    private ContractCoinMatchFactory matchFactory;
    @Autowired
    private ContractCoinService contractCoinService;
    @Autowired
    private ContractMarketService contractMarketService;

    public static String PERIOD[] ={ "1min", "5min", "15min", "30min", "60min","4hour", "1day", "1mon", "1week" };

    private List<CoinSyncItem> coinList = new ArrayList<CoinSyncItem>(); 

    @Value("${platformCoins}")
    private String platformCoins;

    @Autowired
    private ContractMarketService marketService;
    @Autowired
    private MarketFeign marketFeign;
    @Autowired
    private ExchangePushJob exchangePushJob;
    @Autowired
    private IFindUtils iFindUtils;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private Executor taskExecutor = Executors.newScheduledThreadPool(50);

    private double VOLUME_PERCENT = 1; 
    

    @XxlJob("syncKLine")
    public void syncKLine(){

        
        if(coinList.size() == 0 || coinList.size() != matchFactory.getMatchMap().size()) this.initCoinList();

        
        Long currentTime = DateUtil.getTimeMillis() / 1000;

        List<CoinSyncItem> needSyncKList = coinList.stream().filter(coin -> coin.getIsIfind() != 1).collect(Collectors.toList());

        
        for(CoinSyncItem coinItem : needSyncKList) {
            taskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    for (String period : PERIOD) {
                        long fromTime = contractMarketService.findMaxTimestamp(coinItem.getSymbol(), period);
                        
                        long timeGap = currentTime - fromTime;
                        if (period.equals("1min") && timeGap >= 60 * 2) { 
                            if (fromTime == 0) {
                                
                                
                                reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                
                                long toTime = fromTime + (timeGap / 60) * 60 - 5;
                                reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("5min") && timeGap >= 60 * (5 + 2)) { 
                            if (fromTime == 0) {
                                
                                reqKLineList(coinItem.getSymbol(), period, currentTime - 5 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / 300) * 300 - 5;
                                
                                reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("15min") && timeGap >= (60 * (15 + 2))) { 
                            if (fromTime == 0) {
                                
                                reqKLineList(coinItem.getSymbol(), period, currentTime - 15 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 15)) * 60 * 15 - 5;
                                reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("30min") && timeGap >= (60 * (30 + 2))) { 
                            if (fromTime == 0) {
                                
                                reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 30)) * 60 * 30 - 5;
                                reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("60min") && timeGap >= (60 * (60 + 2))) { 
                            if (fromTime == 0) {
                                
                                reqKLineList(coinItem.getSymbol(), period, currentTime - 60 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 60)) * 60 * 60 - 5;
                                reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("4hour") && timeGap >= (60 * 60 * (4 + 2))) { 
                            if (fromTime == 0) {
                                
                                reqKLineList(coinItem.getSymbol(), period, currentTime - 4 * 60 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 60 * 4)) * 60 * 60 * 4 - 5;
                                reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("1day") && timeGap >= (60 * 60 * (24 + 2))) { 
                            if (fromTime == 0) {
                                
                                reqKLineList(coinItem.getSymbol(), period, currentTime - 24 * 60 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 60 * 24)) * 60 * 60 * 24 - 5;
                                reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("1week") && timeGap >= (60 * 60 * 24 * (7 + 2))) { 
                            if (fromTime == 0) {
                                
                                reqKLineList(coinItem.getSymbol(), period, currentTime - 7 * 24 * 60 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 60 * 4 * 7)) * 60 * 60 * 4 * 7 - 5;
                                reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("1mon") && timeGap >= (60 * 60 * 24 * (30 + 2))) { 
                            if (fromTime == 0) {
                                
                                reqKLineList(coinItem.getSymbol(), period, currentTime - 30 * 24 * 60 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 60 * 4 * 30)) * 60 * 60 * 4 * 30 - 5;
                                reqKLineList(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime);
                            }
                        }
                    }

                }
            });
        }
    }


    @XxlJob("syncKLine4PlatForm")
    public void syncKLine4PlatForm(){
        
        if(coinList.size() == 0 || coinList.size() != matchFactory.getMatchMap().size()) this.initCoinList();
        
        Long currentTime = DateUtil.getTimeMillis() / 1000;
        
        for(CoinSyncItem coinItem : coinList) {
            boolean isPlate = false;
            List<String> platformCoinList =  Arrays.stream(platformCoins.split(",")).collect(Collectors.toList());
            if(platformCoinList.contains(coinItem.getSymbol().split("/")[0]) || platformCoinList.contains(coinItem.getSymbol().split("/")[1])){
                isPlate = true;
            }
            if(!isPlate){
                continue;
            }
            
            taskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    for(String period : PERIOD) {
                        
                        long fromTime = contractMarketService.findMaxTimestamp(coinItem.getSymbol(), period);
                        
                        long timeGap = currentTime - fromTime;
                        if(period.equals("1min") && timeGap >= 60) { 
                            if(fromTime == 0) {
                                
                                
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, currentTime - 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            }else{
                                
                                
                                long toTime = fromTime + (timeGap / 60) * 60 -5 ;
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime);
                            }
                        }

                        if(period.equals("5min") && timeGap >= 60 * (5)) { 
                            if(fromTime == 0) {
                                
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, currentTime - 5 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            }else{
                                
                                long toTime = fromTime + (timeGap / 300) * 300  -5;
                                
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime);
                            }
                        }

                        if(period.equals("15min") && timeGap >= (60 * (15))) { 
                            if(fromTime == 0) {
                                
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, currentTime - 15 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            }else{
                                
                                long toTime = fromTime + (timeGap / (60 * 15)) * 60 * 15  -5;
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime);
                            }
                        }

                        if(period.equals("30min") && timeGap >= (60 * (30))) { 
                            if(fromTime == 0) {
                                
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, currentTime - 30 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            }else{
                                
                                long toTime = fromTime + (timeGap / (60 * 30)) * 60 * 30  -5;
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime );
                            }
                        }

                        if(period.equals("60min") && timeGap >= (60 * (60))) { 
                            if(fromTime == 0) {
                                
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, currentTime - 60 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            }else{
                                
                                long toTime = fromTime + (timeGap / (60 * 60)) * 60 * 60  -5;
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime);
                            }
                        }

                        if(period.equals("4hour") && timeGap >= (60 * 60 * (4))) { 
                            if(fromTime == 0) {
                                
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, currentTime - 4 * 60 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            }else{
                                
                                long toTime = fromTime + (timeGap / (60 * 60 * 4)) * 60 * 60 * 4  -5;
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime );
                            }
                        }

                        if(period.equals("1day") && timeGap >= (60 * 60 * (24))) { 
                            if(fromTime == 0) {
                                
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, currentTime - 24 * 60 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            }else{
                                
                                long toTime = fromTime + (timeGap / (60 * 60 * 24)) * 60 * 60 * 24  -5;
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime );
                            }
                        }

                        if(period.equals("1week") && timeGap >= (60 * 60 * 24 * (7))) { 
                            if(fromTime == 0) {
                                
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, currentTime - 7 * 24 * 60 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            }else{
                                
                                long toTime = fromTime + (timeGap / (60 * 60 * 4 * 7)) * 60 * 60 * 4 * 7  -5;
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime);
                            }
                        }

                        if(period.equals("1mon") && timeGap >= (60 * 60 * 24 * (30))) { 
                            if(fromTime == 0) {
                                
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, currentTime - 30 * 24 * 60 * 60 * 600, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            }else{
                                
                                long toTime = fromTime + (timeGap / (60 * 60 * 4 * 30)) * 60 * 60 * 4 * 30  -5;
                                reqKLineList4PlatForm(coinItem.getSymbol(), period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime);
                            }
                        }
                    }
                }
            });

        }
    }


    @XxlJob("syncKLine4Ifind")
    public void syncKLine4Ifind(){

        
        if(coinList.size() == 0 || coinList.size() != matchFactory.getMatchMap().size()) this.initCoinList();

        
        Long currentTime = DateUtil.getTimeMillis() / 1000;

        List<CoinSyncItem> needSyncKList = coinList.stream().filter(coin -> coin.getIsIfind() == 1).collect(Collectors.toList());

        
        
        for(CoinSyncItem coinItem : needSyncKList) {
            
            taskExecutor.execute(new Runnable() {
                @Override
                public void run() {

                    for (String period : PERIOD) {
                        long fromTime = contractMarketService.findMaxTimestamp(coinItem.getSymbol(), period);
                        
                        long timeGap = currentTime - fromTime;
                        if (period.equals("1min") && timeGap >= 60 * 2) { 
                            if (fromTime == 0) {
                                
                                
                                reqIfindKLineList(coinItem, period, currentTime - 60 * 60 * 24 * 2, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                
                                long toTime = fromTime + (timeGap / 60) * 60 - 5;
                                reqIfindKLineList(coinItem,period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }


                        if (period.equals("5min") && timeGap >= 60 * (5 + 1)) { 
                            if (fromTime == 0) {
                                
                                reqIfindKLineList(coinItem, period, 0, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / 300) * 300 - 5;
                                
                                reqIfindKLineList(coinItem, period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("15min") && timeGap >= (60 * (15 + 1))) { 
                            if (fromTime == 0) {
                                
                                reqIfindKLineList(coinItem,period, 0, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 15)) * 60 * 15 - 5;
                                reqIfindKLineList(coinItem,period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("30min") && timeGap >= (60 * (30 + 1))) { 
                            if (fromTime == 0) {
                                
                                reqIfindKLineList(coinItem,period, 0, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 30)) * 60 * 30 - 5;
                                reqIfindKLineList(coinItem,period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("60min") && timeGap >= (60 * (60 + 1))) { 
                            if (fromTime == 0) {
                                
                                reqIfindKLineList(coinItem, period, 0, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 60)) * 60 * 60 - 5;
                                reqIfindKLineList(coinItem, period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("4hour") && timeGap >= (60 * 60 * 4 + 60 )) { 
                            if (fromTime == 0) {
                                
                                reqIfindKLineList(coinItem, period, 0, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 60 * 4)) * 60 * 60 * 4 - 5;
                                reqIfindKLineList(coinItem, period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("1day") && timeGap >= (60 * 60 * 24 + 60)) { 
                            if (fromTime == 0) {
                                
                                reqIfindKLineList(coinItem, period, 0, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 60 * 24)) * 60 * 60 * 24 - 5;
                                reqIfindKLineList(coinItem,period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }

                        if (period.equals("1week") && timeGap >= (60 * 60 * 24 * 7 + 60)) { 
                            if (fromTime == 0) {
                                
                                reqIfindKLineList(coinItem, period, 0, currentTime);
                                coinItem.setLastPeriodTime(period, currentTime);
                            } else {
                                
                                long toTime = fromTime + (timeGap / (60 * 60 * 4 * 7)) * 60 * 60 * 4 * 7 - 5;
                                reqIfindKLineList(coinItem, period, fromTime, toTime);
                                coinItem.setLastPeriodTime(period, toTime - 10);
                            }
                        }
                    }

                }
            });
        }
    }



    @XxlJob("syncPlate4Ifind")
    public void syncPlate4Ifind(){

        
        if(coinList.size() == 0 || coinList.size() != matchFactory.getMatchMap().size()) this.initCoinList();

        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        String currencyJson = opsForValue.get(SysConstant.CURRENCY);
        BigDecimal rate = BigDecimal.valueOf(7L);
        if(currencyJson!=null && !"".equals(currencyJson)){
            List<Currency> currencyList = JSON.parseArray(currencyJson,Currency.class);
            for (Currency currency : currencyList) {
                if("CNY".equals(currency.getFullName())){
                    rate = currency.getRate();
                }
            }
        }

        
        List<CoinSyncItem> needSyncKList = coinList.stream().filter(coin -> coin.getIsIfind() == 1).collect(Collectors.toList());
        if(needSyncKList!=null && needSyncKList.size()>0){
            Map<String,CoinSyncItem> codeSymbolMap = needSyncKList.stream().collect(Collectors.toMap(CoinSyncItem::getIfindCode, Function.identity()));
                    
            List<String> collect = needSyncKList.stream().map(coinSyncItem -> coinSyncItem.getIfindCode()).collect(Collectors.toList());
            String codes = String.join(",", collect);
            try {
                JSONObject jsonObject = iFindUtils.getRealTimePlate(codes);
                if(jsonObject!=null){
                    JSONArray tables = jsonObject.getJSONArray("tables");
                    if(tables!=null && tables.size()>0){
                        for (int i = 0; i < tables.size(); i++) {
                            JSONObject result = tables.getJSONObject(i);
                            String code = result.getString("thscode");
                            CoinSyncItem coinItem = codeSymbolMap.get(code);
                            List<TradePlateItem> buyItems = new ArrayList<>();
                            List<TradePlateItem> sellItems = new ArrayList<>();
                            JSONObject table = result.getJSONObject("table");


                            
                            JSONArray latestPriceArray = table.getJSONArray("latest");
                            JSONArray volumeArray = table.getJSONArray("volume");
                            if(latestPriceArray!=null && latestPriceArray.size()>0){
                                BigDecimal latest = latestPriceArray.getBigDecimal(0);
                                if(coinItem.getNeedConvert()==1){
                                    latest = latest.divide(rate,2,BigDecimal.ROUND_HALF_DOWN);
                                }
                                BigDecimal volume = volumeArray.getBigDecimal(0);
                                
                                createTradesByLatest(coinItem.getSymbol(),latest,volume);
                            }

                            
                            for (int j = 1; j <=10 ; j++) {
                                String key = "bid"+j;
                                String keySize = "bidSize"+j;
                                JSONArray bidArray = table.getJSONArray(key);
                                JSONArray bidSizeArray = table.getJSONArray(keySize);
                                if(bidArray!=null && bidArray.size()>0){
                                    BigDecimal price = bidArray.getBigDecimal(0);
                                    if(coinItem.getNeedConvert()==1){
                                        price = price.divide(rate,2,BigDecimal.ROUND_HALF_DOWN);
                                    }
                                    BigDecimal amount = bidSizeArray.getBigDecimal(0);
                                    TradePlateItem  item = new TradePlateItem();
                                    item.setPrice(price);
                                    item.setAmount(amount);
                                    buyItems.add(item);
                                }

                                String akey = "ask"+j;
                                String akeySize = "askSize"+j;
                                JSONArray askArray = table.getJSONArray(akey);
                                JSONArray askSizeArray = table.getJSONArray(akeySize);
                                if(askArray!=null && askArray.size()>0){
                                    BigDecimal price = askArray.getBigDecimal(0);
                                    if(coinItem.getNeedConvert()==1){
                                        price = price.divide(rate,2,BigDecimal.ROUND_HALF_DOWN);
                                    }
                                    BigDecimal amount = askSizeArray.getBigDecimal(0);
                                    TradePlateItem  item = new TradePlateItem();
                                    item.setPrice(price);
                                    item.setAmount(amount);
                                    sellItems.add(item);
                                }
                            }

                            if(sellItems.size()>0 || buyItems.size()>0){
                                
                                this.matchFactory.getContractCoinMatch(coinItem.getSymbol()).refreshPlate(buyItems, sellItems);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void createTradesByLatest(String symbol, BigDecimal latest,BigDecimal volume) {

        List<ContractTrade> tradeArrayList = new ArrayList<ContractTrade>();
        List<Poke> pokes = marketService.findPokeAndRemove(symbol,"trade",null);
        int size = new Random().nextInt(5);
            for (int i = 0;i<size;i++) {
                BigDecimal amount = BigDecimal.valueOf(new Random().nextInt(volume.intValue()));
                BigDecimal price =  latest;
                
                ContractTrade trade = new ContractTrade();
                trade.setAmount(amount);
                trade.setPrice(price);
                if(new Random().nextInt(2) == 0) {
                    trade.setDirection(ContractOrderDirection.BUY);
                    trade.setBuyOrderId("IF-----");
                    trade.setBuyTurnover(amount.multiply(price));
                }else{
                    trade.setDirection(ContractOrderDirection.SELL);
                    trade.setSellOrderId("IF----");
                    trade.setSellTurnover(amount.multiply(price));
                }
                trade.setSymbol(symbol);
                trade.setTime(new Date().getTime());
                tradeArrayList.add(trade);
            }

            if(pokes!=null && pokes.size()>0){
                for (int i = 0; i < pokes.size(); i++) {

                    BigDecimal amount = BigDecimal.valueOf(new Random().nextInt(volume.intValue()));
                    BigDecimal price = BigDecimal.valueOf(Double.parseDouble(pokes.get(i).getPrice()));
                    
                    ContractTrade trade = new ContractTrade();
                    trade.setAmount(amount);
                    trade.setPrice(price);
                    if(new Random().nextInt(2) == 0) {
                        trade.setDirection(ContractOrderDirection.BUY);
                        trade.setBuyOrderId("IF-----");
                        trade.setBuyTurnover(amount.multiply(price));
                    }else{
                        trade.setDirection(ContractOrderDirection.SELL);
                        trade.setSellOrderId("IF----");
                        trade.setSellTurnover(amount.multiply(price));
                    }
                    trade.setSymbol(symbol);
                    trade.setTime(new Date().getTime());
                    tradeArrayList.add(trade);
                }
            }
            
            this.matchFactory.getContractCoinMatch(symbol).refreshLastedTrade(tradeArrayList);
            this.matchFactory.refreshPrice(symbol,latest);
    }


    private void reqIfindKLineList(CoinSyncItem coinItem, String period, long from, long to) {
        String symbol = coinItem.getSymbol();
        String codes = coinItem.getIfindCode();
        int needConvert = coinItem.getNeedConvert();
        if("1mon".equals(period)){
            return;
        }
        List<KLine> allKLine = marketService.findAllKLine(symbol, from, to, period);
        List<Long> klineTimes = allKLine.stream().map(x -> x.getTime()).collect(Collectors.toList());
        

        List<KLine> klineList = new ArrayList<>();
        String fromStr = DateUtil.getDateStr(from*1000, "yyyy-MM-dd HH:mm:ss");
        String endStr = DateUtil.getDateStr(to*1000, "yyyy-MM-dd HH:mm:ss");

        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        String currencyJson = opsForValue.get(SysConstant.CURRENCY);
        BigDecimal rate = BigDecimal.valueOf(7L);
        if(currencyJson!=null && !"".equals(currencyJson)){
            List<Currency> currencyList = JSON.parseArray(currencyJson,Currency.class);
            for (Currency currency : currencyList) {
                if("CNY".equals(currency.getFullName())){
                    rate = currency.getRate();
                }
            }
        }

        if("1min".equals(period)){
            try {
                JSONObject jsonObject = iFindUtils.getKLineMinute(codes,fromStr,endStr,"1");
                if(jsonObject!=null){
                    JSONArray tables = jsonObject.getJSONArray("tables");
                    if(tables!=null && tables.size()>0){
                        JSONObject result = tables.getJSONObject(0);
                        JSONArray times = result.getJSONArray("time");
                        if(times!=null && times.size()>0){
                            JSONObject table = result.getJSONObject("table");
                            JSONArray opens = table.getJSONArray("open");
                            JSONArray closes = table.getJSONArray("close");
                            JSONArray highs = table.getJSONArray("high");
                            JSONArray lows = table.getJSONArray("low");
                            JSONArray volumes = table.getJSONArray("volume");
                            JSONArray amounts = table.getJSONArray("amount");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            for (int i = 0;i<times.size();i++) {
                                Object time = times.get(i);
                                KLine  kline = new KLine();
                                kline.setPeriod(period);
                                Date parse = sdf.parse(time.toString());
                                kline.setTime(parse.getTime()/1000);
                                kline.setOpenPrice(opens.getBigDecimal(i));
                                kline.setClosePrice(closes.getBigDecimal(i));
                                kline.setHighestPrice(highs.getBigDecimal(i));
                                kline.setLowestPrice(lows.getBigDecimal(i));
                                if(needConvert==1){
                                    kline.setOpenPrice(opens.getBigDecimal(i).divide(rate,2,BigDecimal.ROUND_HALF_DOWN));
                                    kline.setClosePrice(closes.getBigDecimal(i).divide(rate,2,BigDecimal.ROUND_HALF_DOWN));
                                    kline.setHighestPrice(highs.getBigDecimal(i).divide(rate,2,BigDecimal.ROUND_HALF_DOWN));
                                    kline.setLowestPrice(lows.getBigDecimal(i).divide(rate,2,BigDecimal.ROUND_HALF_DOWN));
                                }

                                kline.setVolume(volumes.getBigDecimal(i));
                                kline.setTurnover(amounts.getBigDecimal(i));
                                klineList.add(kline);

                            }

                        }

                    }
                }

            } catch (Exception e) {
               e.printStackTrace();
            }
        }else {
            
            List<KLine> result = generatorKLineByPeriod(symbol, period, from, to);
            if(result!=null){
                klineList = result;
            }
        }

        
        List<KLine> lines = new LinkedList<>();
        
        KLine lastKLine = null;
        for(int i = 0; i < klineList.size(); i++) {
            KLine klineObj = klineList.get(i);
            long time = klineObj.getTime();
            if(klineTimes.contains(time)){
                
                continue;
            }
            BigDecimal open = klineObj.getOpenPrice() ; 
            BigDecimal close = klineObj.getClosePrice() ;
            BigDecimal high =  klineObj.getHighestPrice();
            BigDecimal low =  klineObj.getLowestPrice();


            KLine kline = new KLine(period);
            kline.setClosePrice(close);
            kline.setCount(klineObj.getCount());
            kline.setHighestPrice(high);
            kline.setLowestPrice(low);
            kline.setOpenPrice(open);
            kline.setTime(time);
            kline.setTurnover(klineObj.getTurnover());
            kline.setVolume(klineObj.getVolume());

            
            if(klineList.size() == 1) {
                lastKLine = kline;
            }else if(klineList.size() > 1){
                if(i == klineList.size() - 1) {
                    lastKLine = kline;
                }
            }

            if(lastKLine!=null){

                List<Poke> pokes = marketService.findPokeAndRemove(symbol,"kline",period);
                BigDecimal lowPokePrice = null;
                BigDecimal highPokePrice = null;
                boolean isHaveP = false;
                if(pokes!=null && pokes.size()>0){
                    for (Poke poke : pokes) {
                        isHaveP = true;
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
                if(isHaveP){
                    if(lastKLine == null){
                        lastKLine = allKLine.get(allKLine.size() - 1);
                    }
                    high = highPokePrice == null ? lastKLine.getHighestPrice() : highPokePrice.compareTo(lastKLine.getHighestPrice()) == 1 ? highPokePrice : lastKLine.getHighestPrice(); 
                    low = lowPokePrice == null ? lastKLine.getLowestPrice() : lowPokePrice.compareTo(lastKLine.getLowestPrice()) == -1 ? lowPokePrice : lastKLine.getLowestPrice(); 
                    lastKLine.setHighestPrice(high);
                    lastKLine.setLowestPrice(low);
                }

                lines.add(lastKLine);
            }

            marketService.saveKLine(symbol, kline);

        }

    }

   private List<KLine> generatorKLineByPeriod(String symbol,String period,Long from,Long to){
        long timeGap = 5 * 60;
        String periodKline = "1min";
        if (period.equals("5min")) {
            timeGap = 5 * 60;
            periodKline = "1min";
        }else if (period.equals("15min")) { 
            timeGap = 15 * 60;
            periodKline = "1min";
        }else if(period.equals("30min")){
            timeGap = 30 * 60;
            periodKline = "1min";

        }else if(period.equals("60min")){
            timeGap = 60 * 60;
            periodKline = "1min";

        }else if(period.equals("4hour")){
            timeGap = 4 * 60 * 60;
            periodKline = "60min";

        }else if(period.equals("1day")){
            timeGap = 24 * 60 * 60;
            periodKline = "60min";

        }else if(period.equals("1week")){
            timeGap = 7 * 24 * 60 * 60;
            periodKline = "1day";
        }





        List<KLine> lines = marketService.findAllKLine(symbol, from, to, periodKline);
        if (lines.size() > 0) {
            
            Map<Long, KLine> klineMap = new HashMap<>();

            for (KLine item : lines) {
                
                long fiveMinuteStartTime = item.getTime() - (item.getTime() % timeGap);

                
                if (!klineMap.containsKey(fiveMinuteStartTime)) {
                    KLine kline = new KLine();
                    kline.setTime(fiveMinuteStartTime);
                    kline.setOpenPrice(item.getOpenPrice()); 
                    kline.setLowestPrice(item.getLowestPrice());
                    kline.setHighestPrice(item.getHighestPrice());
                    kline.setVolume(item.getVolume());
                    kline.setTurnover(item.getTurnover());
                    kline.setCount(item.getCount());
                    kline.setClosePrice(item.getClosePrice()); 
                    klineMap.put(fiveMinuteStartTime, kline);
                } else {
                    
                    KLine kline = klineMap.get(fiveMinuteStartTime);
                    kline.setHighestPrice(kline.getHighestPrice().max(item.getHighestPrice()));
                    kline.setLowestPrice(kline.getLowestPrice().min(item.getLowestPrice()));
                    kline.setVolume(kline.getVolume().add(item.getVolume()));
                    kline.setTurnover(kline.getTurnover().add(item.getTurnover()));
                    kline.setCount(kline.getCount() + item.getCount());
                    kline.setClosePrice(item.getClosePrice()); 
                }
            }

            
            List<KLine> result = new ArrayList<>(klineMap.values());

            
            result.sort(Comparator.comparingLong(KLine::getTime));

            
            return result;

        }
        return null;


    }

    


    public void checkNewCoin(){

    }

    private void reqKLineList(String symbol, String period, long from, long to) {
        
        List<String> platformCoinList =  Arrays.stream(platformCoins.split(",")).collect(Collectors.toList());
        if(platformCoinList.contains(symbol.split("/")[0]) || platformCoinList.contains(symbol.split("/")[1])){


        }else {
            HuobiWebSocketConnectionManage.getWebSocket().reqKLineList(symbol, period, from, to);
        }
    }


    private void reqKLineList4PlatForm(String symbol, String period, long from, long to) {
        
        List<String> platformCoinList =  Arrays.stream(platformCoins.split(",")).collect(Collectors.toList());
        if(platformCoinList.contains(symbol.split("/")[0]) || platformCoinList.contains(symbol.split("/")[1])){
            
            saveKLine4Platform(symbol, period, from, to);
            
        }else {

        }
    }


    private void saveKLine4Platform(String symbol,String period,Long from, Long to){

        List<KLine> allKLine = marketService.findAllKLine(symbol, from, to, period);
        List<Long> times = allKLine.stream().map(x -> x.getTime()).collect(Collectors.toList());
        from = from*1000;
        to = to*1000;
        
        String period4Feign = period;
        if("60min".equals(period)){
            period4Feign = "1hour";
        }
        List<KLine> klineList = marketFeign.findKHistory4Feign(symbol,from,to,period4Feign);
        
        List<KLine> lines = new LinkedList<>();
        

        KLine lastKLine = null;
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


            KLine kline = new KLine(period);
            kline.setClosePrice(close);
            kline.setCount(klineObj.getCount());
            kline.setHighestPrice(high);
            kline.setLowestPrice(low);
            kline.setOpenPrice(open);
            kline.setTime(time);
            kline.setTurnover(klineObj.getTurnover());
            kline.setVolume(klineObj.getVolume());

            
            if(klineList.size() == 1) {
                lastKLine = kline;
            }else if(klineList.size() > 1){
                if(i == klineList.size() - 1) {
                    lastKLine = kline;
                }
            }

            if(lastKLine!=null){

                List<Poke> pokes = marketService.findPokeAndRemove(symbol,"kline",period);
                BigDecimal lowPokePrice = null;
                BigDecimal highPokePrice = null;
                boolean isHaveP = false;
                if(pokes!=null && pokes.size()>0){
                    for (Poke poke : pokes) {
                        isHaveP = true;
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
                if(isHaveP){
                    if(lastKLine == null){
                        lastKLine = allKLine.get(allKLine.size() - 1);
                    }
                    high = highPokePrice == null ? lastKLine.getHighestPrice() : highPokePrice.compareTo(lastKLine.getHighestPrice()) == 1 ? highPokePrice : lastKLine.getHighestPrice(); 
                    low = lowPokePrice == null ? lastKLine.getLowestPrice() : lowPokePrice.compareTo(lastKLine.getLowestPrice()) == -1 ? lowPokePrice : lastKLine.getLowestPrice(); 
                    lastKLine.setHighestPrice(high);
                    lastKLine.setLowestPrice(low);



                }


                lines.add(lastKLine);


            }


            marketService.saveKLine(symbol, kline);








        }








        
        if(lastKLine!=null){


            String period1 = lastKLine.getPeriod();
            ContractCoinMatch match = matchFactory.getContractCoinMatch(symbol);
            BigDecimal lastPrice = match.getNowPrice();
            if(period1.endsWith("min")){
                Long end =  getMinTime(period1.replace("min",""),0);
                Long start =  getMinTime(period1.replace("min",""),-1);
                if(end > lastKLine.getTime()){
                    KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKLine, start,end);
                    

                    lines.add(newKLine);
                }
            }
            if(period1.endsWith("hour")){
                Long end =  getHourTime(period1.replace("hour",""),0);
                Long start =  getHourTime(period1.replace("hour",""),-1);
                if(end > lastKLine.getTime()){
                    KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKLine, start,end);

                    lines.add(newKLine);
                }
            }
            if(period1.endsWith("day")){
                Long end =  getHourTime(period1.replace("day",""),0);
                Long start =  getHourTime(period1.replace("day",""),-1);
                if(end > lastKLine.getTime()){
                    KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKLine, start,end);

                    lines.add(newKLine);
                }
            }
            exchangePushJob.pushTickKline(symbol, lines);
        }




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
            List<ContractCoin> contractCoinList = contractCoinService.findAllEnabled();
            
            for(ContractCoin coin : contractCoinList) {
                String symbol = coin.getSymbol();
                CoinSyncItem coinItem = new CoinSyncItem(PERIOD);
                coinItem.setSymbol(symbol);
                coinItem.setIfindCode(coin.getIfindCode());
                coinItem.setIsIfind(coin.getIsIfind());
                coinItem.setNeedConvert(coin.getNeedConvert());
                
                for(String period : PERIOD){
                    long lastTime = contractMarketService.findMaxTimestamp(symbol, period);
                    coinItem.setLastPeriodTime(period, lastTime);

                    if(lastTime == 0 && coin.getIsIfind()==0) {
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
                    }else if(lastTime == 0 && coin.getIsIfind()==1){
                        Long currentTime = DateUtil.getTimeMillis() / 1000;

                        if(period.equals("1min")) this.reqIfindKLineList(coinItem, period, currentTime - 60 * 600, currentTime);
                        if(period.equals("5min")) this.reqIfindKLineList(coinItem,period, currentTime - 5 * 60 * 600, currentTime);
                        if(period.equals("15min")) this.reqIfindKLineList(coinItem, period, currentTime - 15 * 60 * 600, currentTime);
                        if(period.equals("30min")) this.reqIfindKLineList(coinItem, period, currentTime - 30 * 60 * 600, currentTime);
                        if(period.equals("60min")) this.reqIfindKLineList(coinItem, period, currentTime - 60 * 60 * 600, currentTime);
                        if(period.equals("4hour")) this.reqIfindKLineList(coinItem,period, currentTime - 4 * 60 * 60 * 600, currentTime);
                        if(period.equals("1day")) this.reqIfindKLineList(coinItem, period, currentTime - 24 * 60 * 60 * 600, currentTime);
                        if(period.equals("1week")) this.reqIfindKLineList(coinItem, period, currentTime - 7 * 24 * 60 * 60 * 600, currentTime);
                        if(period.equals("1mon")) this.reqIfindKLineList(coinItem,period, currentTime - 30 * 24 * 60 * 60 * 600, currentTime);

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
                    ContractCoin bySymbol = contractCoinService.findBySymbol(symbol);
                    cs.setSymbol(symbol);
                    cs.setIfindCode(bySymbol.getIfindCode());
                    cs.setIsIfind(bySymbol.getIsIfind());
                    cs.setNeedConvert(bySymbol.getNeedConvert());
                    
                    for(String p : PERIOD){
                        long lastTime = contractMarketService.findMaxTimestamp(symbol, p);
                        cs.setLastPeriodTime(p, lastTime);
                    }
                    
                    coinList.add(cs);
                }
            }
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
        System.out.println(calendar.getTime().getTime());
        return calendar.getTime().getTime()/1000;
    }

    class CoinSyncItem{
        String symbol;
        Map<String, Long> lastUpdateTime;

        String ifindCode;

        int isIfind = 0;

        int needConvert = 0;

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

        public int getIsIfind() {
            return isIfind;
        }

        public void setIsIfind(int isIfind) {
            this.isIfind = isIfind;
        }

        public String getIfindCode() {
            return ifindCode;
        }

        public void setIfindCode(String ifindCode) {
            this.ifindCode = ifindCode;
        }

        public int getNeedConvert() {
            return needConvert;
        }

        public void setNeedConvert(int needConvert) {
            this.needConvert = needConvert;
        }
    }
}
