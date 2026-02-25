package com.wikex.wikex.swap.consumer;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.pojo.ContractTrade;
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.Poke;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.job.ExchangePushJob;
import com.wikex.wikex.swap.service.ContractMarketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Component
@RocketMQMessageListener(topic = "market-kLine", consumerGroup = "market-kLine-swap")
public class MarketKLineConsumer implements RocketMQListener<String> {

    private ArrayList<String> subCoinList = new ArrayList<String>();

    @Autowired
    private ContractCoinMatchFactory matchFactory;
    @Autowired
    private ContractMarketService marketService;

    @Autowired
    private ExchangePushJob exchangePushJob;
    private double VOLUME_PERCENT = 1;

    @Value("${platformCoins}")
    private String platformCoins;

    @Override
    public void onMessage(String content) {
        
        if (content == null) {
            return;
        }
        Map pMap = JSON.parseObject(content, Map.class);
        if (pMap == null) {
            return;
        }
        String tSymbol = pMap.get("symbol") != null ? pMap.get("symbol").toString() : "";
        if (null != this.matchFactory.getMatchMap() && this.matchFactory.getMatchMap().size() > 0) {
            
            for (String symbol : this.matchFactory.getMatchMap().keySet()) {
                
                if (platformCoins.contains(symbol.split("/")[0]) || platformCoins.contains(symbol.split("/")[1])) {
                    if (!subCoinList.contains(symbol)) {
                        subCoinList.add(symbol);
                    }
                }
            }
            if (subCoinList.size() > 0) {
                for (String symbol : subCoinList) {
                    if (symbol.equalsIgnoreCase(tSymbol)) {
                        Object kLine = pMap.get("kLine");
                        if (kLine == null) {
                            continue;
                        }
                        KLine lastKLine = JSON.parseObject(JSON.toJSONString(kLine), KLine.class);
                        if(lastKLine!=null && "1hour".equals(lastKLine.getPeriod())){
                            lastKLine.setPeriod("60min");
                        }
                        long time = lastKLine.getTime()/1000;
                        lastKLine.setTime(time);
                        List<KLine> allKLine = marketService.findKLineByTime(symbol, time, lastKLine.getPeriod());
                        if (allKLine != null && allKLine.size() > 0) {
                            
                            continue;
                        }

                        if(lastKLine!=null){
                            List<Poke> pokes = marketService.findPokeAndRemove(symbol,"kline",lastKLine.getPeriod());
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
                                BigDecimal high = highPokePrice == null ? lastKLine.getHighestPrice() : highPokePrice.compareTo(lastKLine.getHighestPrice()) == 1 ? highPokePrice : lastKLine.getHighestPrice(); 
                                BigDecimal low = lowPokePrice == null ? lastKLine.getLowestPrice() : lowPokePrice.compareTo(lastKLine.getLowestPrice()) == -1 ? lowPokePrice : lastKLine.getLowestPrice(); 
                                lastKLine.setHighestPrice(high);
                                lastKLine.setLowestPrice(low);
                            }
                        }
                        marketService.saveKLine(symbol, lastKLine);

                        
                        if(lastKLine!=null) {
                            List<KLine> lines = new LinkedList<>();
                            lines.add(lastKLine);
                            String period1 = lastKLine.getPeriod();
                            ContractCoinMatch match = matchFactory.getContractCoinMatch(symbol);
                            BigDecimal lastPrice = match.getNowPrice();
                            if (period1.endsWith("min")) {
                                Long end = getMinTime(period1.replace("min", ""), 0);
                                Long start = getMinTime(period1.replace("min", ""), -1);
                                if (end > lastKLine.getTime()) {
                                    KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKLine, start, end);
                                    
                                    lines.add(newKLine);
                                }
                            }
                            if (period1.endsWith("hour")) {
                                Long end = getHourTime(period1.replace("hour", ""), 0);
                                Long start = getHourTime(period1.replace("hour", ""), -1);
                                if (end > lastKLine.getTime()) {
                                    KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKLine, start, end);
                                    lines.add(newKLine);
                                }
                            }
                            if (period1.endsWith("day")) {
                                Long end = getHourTime(period1.replace("day", ""), 0);
                                Long start = getHourTime(period1.replace("day", ""), -1);
                                if (end > lastKLine.getTime()) {
                                    KLine newKLine = createNewTempLastKLine(symbol, period1, lastPrice, lastKLine, start, end);
                                    lines.add(newKLine);
                                }
                            }
                            exchangePushJob.pushTickKline(symbol, lines);
                        }
                    }

                }
            }
        }
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

    private KLine createNewTempLastKLine(String symbol, String period, BigDecimal lastPrice, KLine temKline, Long start,Long end) {























        
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

}
