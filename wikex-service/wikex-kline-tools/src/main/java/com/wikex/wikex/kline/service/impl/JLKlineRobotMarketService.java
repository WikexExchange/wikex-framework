package com.wikex.wikex.kline.service.impl;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.kline.entity.KLine;
import com.wikex.wikex.kline.entity.Symbol;
import com.wikex.wikex.kline.service.KlineRobotMarketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class JLKlineRobotMarketService implements KlineRobotMarketService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private Logger logger = LoggerFactory.getLogger(JLKlineRobotMarketService.class);


//    public static String PERIOD[] ={ "1min", "5min", "15min", "30min", "60min", "1day", "1mon", "1week" };
    @Override
    public void saveKLine(String symbol, KLine kLine){
        String period = kLine.getPeriod();
        if(period.equals("60min")){
            period = "1h";
        }else if(period.equals("1day")){
            kLine.setTime(kLine.getTime()+60*60*8*1000);
            period = "1d";
        }else if(period.equals("1min")){
            period = "1m";
        }
        kLine.setPeriod(period);
        List<String> list = new ArrayList<>();
        list.add(kLine.getOpenPrice().setScale(8, RoundingMode.HALF_UP).toPlainString());
        list.add(kLine.getClosePrice().setScale(8,RoundingMode.HALF_UP).toPlainString());
        list.add(kLine.getHighestPrice().setScale(8,RoundingMode.HALF_UP).toPlainString());
        list.add(kLine.getLowestPrice().setScale(8,RoundingMode.HALF_UP).toPlainString());
        list.add(kLine.getTurnover().setScale(8,RoundingMode.HALF_UP).toPlainString());
        list.add(kLine.getVolume().setScale(8,RoundingMode.HALF_UP).toPlainString());
        redisTemplate.boundHashOps("k:"+symbol.replace("/","")+":"+period).put(kLine.getTime()/1000+"", JSON.toJSON(list).toString());

    }

    public static void main(String[] args) {
        System.out.println(1608048000+60*60*8);
    }

    /**
     * @param symbol
     * @param period
     * @return
     */
    @Override
    public long findMaxTimestamp(String symbol, String period) {
        return 0;
    }

    @Override
    public List<Symbol> findAllSymbol() {
        return null;
    }

    @Override
    public void addSymbol(Symbol symbol) {
    }

    @Override
    public void deleteAll(String symbol) {

    }
}
