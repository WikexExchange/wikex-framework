package com.wikex.wikex.swap.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.pojo.*;
import com.wikex.wikex.swap.engine.ContractCoinMatch;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.ContractMarketService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.util.*;

@Api(tags = "Trading")
@Slf4j
@RestController
public class MarketController {

    @Autowired
    private ContractCoinService coinService;

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;

    @Autowired
    private ContractMarketService marketService;


    /**
     * Get supported trading symbols
     * @return
     */
    @ApiOperation(value = "Get supported trading symbols")
    @RequestMapping("symbol")
    public List<ContractCoin> findAllSymbol(){
        List<ContractCoin> coins = coinService.findAllVisible();
        return coins;
    }

    /**
     * Get coin thumbnail market data
     * @return
     */
    @ApiOperation(value = "Get coin thumbnail market data")
    @RequestMapping("symbol-thumb")
    public JSONArray findSymbolThumb(){
        List<ContractCoin> coins = coinService.findAllVisible();
//        List<CoinThumb> thumbs = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        // Set seconds and milliseconds to 0
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        calendar.set(Calendar.MINUTE,0);
        long nowTime = calendar.getTimeInMillis();
        calendar.add(Calendar.HOUR_OF_DAY,-24);
        JSONArray array = new JSONArray();
        long firstTimeOfToday = calendar.getTimeInMillis();
        for(ContractCoin coin:coins){
            ContractCoinMatch processor = contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol());
            CoinThumb thumb = processor.getThumb();
            thumb.setFeePercent(coin.getFeePercent());
            JSONObject json = (JSONObject) JSON.toJSON(thumb);
            List<KLine> lines = marketService.findAllKLine(thumb.getSymbol(),firstTimeOfToday,nowTime,"1hour");
            JSONArray trend = new JSONArray();
            for(KLine line:lines){
                trend.add(line.getClosePrice());
            }
            json.put("trend",trend);
            json.put("isIfind",coin.getIsIfind());
            array.add(json);
//            thumbs.add(thumb);
        }
//        return thumbs;
        return array;
    }

    /**
     * Query latest trade records
     * @param symbol Trading pair symbol
     * @param size Max number of records to return
     * @return
     */
    @ApiOperation(value = "Query latest trade records")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
            @ApiImplicitParam(name = "size", value = "Max number of records to return"),
    })
    @RequestMapping("latest-trade")
    public List<ContractTrade> latestTrade(String symbol, Integer size){
        ContractCoinMatch match = contractCoinMatchFactory.getContractCoinMatch(symbol);
        if (match == null) {
            return null;
        }
        return match.getLastedTradeList();
    }

    /**
     * Get details for a trading pair
     * @param symbol
     * @return
     */
    @ApiOperation(value = "Get details for a trading pair")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("symbol-info")
    public ContractCoin findSymbol(String symbol){
        ContractCoin coin = coinService.findBySymbol(symbol);
        coin.setCurrentTime(Calendar.getInstance().getTimeInMillis());
        coin.setLeftTime(this.getLeftTime());
        return coin;
    }

    private Long getLeftTime(){
        long currentTimeMillis = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long millis8 = cal.getTimeInMillis()-currentTimeMillis;
        cal.set(Calendar.HOUR_OF_DAY, 16);
        long millis16 = cal.getTimeInMillis()-currentTimeMillis;
        cal.set(Calendar.HOUR_OF_DAY, 24);
        long millis24 = cal.getTimeInMillis()-currentTimeMillis;

        if(millis8>0){
            return millis8/1000;
        }else if(millis16>0){
            return millis16/1000;
        }else {
            return millis24/1000;
        }
    }

    @ApiOperation(value = "Get details for a trading pair")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("exchange-plate")
    public Map<String,List<TradePlateItem>> findTradePlate(String symbol){
        Map<String, List<TradePlateItem>> result = new HashMap<>();
        ContractCoinMatch match = contractCoinMatchFactory.getContractCoinMatch(symbol);
        if (match == null) {
            return null;
        }
        result.put("bid", match.getTradePlate(ContractOrderDirection.BUY).getItems());
        result.put("ask", match.getTradePlate(ContractOrderDirection.SELL).getItems());

        return result;
    }

    @ApiOperation(value = "Get details for a trading pair (mini)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("exchange-plate-mini")
    public Map<String, JSONObject> findTradePlateMini(String symbol){
        Map<String, JSONObject> result = new HashMap<>();
        ContractCoinMatch match = contractCoinMatchFactory.getContractCoinMatch(symbol);
        if (match == null) {
            return null;
        }
        result.put("bid", match.getTradePlate(ContractOrderDirection.BUY).toJSON(20));
        result.put("ask", match.getTradePlate(ContractOrderDirection.SELL).toJSON(20));

        return result;
    }

    @ApiOperation(value = "Get details for a trading pair (full)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("exchange-plate-full")
    public Map<String,JSONObject> findTradePlateFull(String symbol){
        Map<String, JSONObject> result = new HashMap<>();
        ContractCoinMatch match = contractCoinMatchFactory.getContractCoinMatch(symbol);
        if (match == null) {
            return null;
        }
        result.put("bid", match.getTradePlate(ContractOrderDirection.BUY).toJSON());
        result.put("ask", match.getTradePlate(ContractOrderDirection.SELL).toJSON());

        return result;
    }

    /**
     * Get historical K-lines for a coin
     * @param symbol
     * @param from
     * @param to
     * @param resolution
     * @return
     */
    @ApiOperation(value = "Get historical K-lines for a coin")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
            @ApiImplicitParam(name = "from", value = "Start time"),
            @ApiImplicitParam(name = "to", value = "End time"),
            @ApiImplicitParam(name = "resolution", value = "Resolution"),
    })
    @RequestMapping("history")
    public JSONArray findKHistory(String symbol, Long from, Long to, String resolution) throws ParseException {
        String period = "";
        boolean isMin = false;
        boolean isHour = false;
        boolean isDay = false;
        if(resolution.endsWith("H") || resolution.endsWith("h")){
            isHour = true;
            period = resolution.substring(0,resolution.length()-1) + "hour";
        }
        else if(resolution.endsWith("D") || resolution.endsWith("d")){
            isDay = true;
            period = resolution.substring(0,resolution.length()-1) + "day";
        }
        else if(resolution.endsWith("W") || resolution.endsWith("w")){
            period = resolution.substring(0,resolution.length()-1) + "week";
        }
        else if(resolution.endsWith("M") || resolution.endsWith("m")){
            period = resolution.substring(0,resolution.length()-1) + "mon";
        }
        else{
            Integer val = Integer.parseInt(resolution);
            if(val <= 60) {
                isMin = true;
                period = resolution + "min";
            }
            else {
                period = (val/60) + "hour";
            }
        }
        if(resolution.equals("240")){
            isHour = true;
        }
        from = from / 1000;
        to = to / 1000;
        ContractCoinMatch match = contractCoinMatchFactory.getContractCoinMatch(symbol);
        BigDecimal lastPrice = match.getNowPrice();
        List<KLine> list = marketService.findAllKLine(symbol,from,to,period);
        
        JSONArray array = new JSONArray();
        boolean startFlag = false;
        KLine temKline = null;
        for (int i = 0; i < list.size(); i++) {
            KLine item = list.get(i);
            item.setTime(item.getTime() * 1000);
            // This section filters K-lines with 0 open/close at the beginning
            if(!startFlag && item.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }else {
                startFlag = true;
            }
            // If 0 appears in the middle, handle it
            if(item.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                item.setOpenPrice(temKline.getClosePrice());
                item.setClosePrice(temKline.getClosePrice());
                item.setHighestPrice(temKline.getClosePrice());
                item.setLowestPrice(temKline.getClosePrice());
            }
            JSONArray group = new JSONArray();
            group.add(0,item.getTime());
            group.add(1,item.getOpenPrice());
            group.add(2,item.getHighestPrice());
            group.add(3,item.getLowestPrice());
            group.add(4,item.getClosePrice());
            group.add(5,item.getVolume());
            array.add(group);

            temKline = item;
        }
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String sDate = df.format(date);
        // Current time point
        long curTime = df.parse(sDate).getTime();
        if(temKline!=null && "1".equals(resolution)) {
            if (temKline.getTime() > curTime) {
                // No need to append
                // Set latest price
                JSONArray group  = (JSONArray)array.get(array.size() - 1);
                group.add(4,lastPrice);
                array.set(array.size() - 1,group);
            } else {
                List<Poke> pokes = marketService.findPoke(symbol, "kline", period);
                BigDecimal lowPokePrice = null;
                BigDecimal highPokePrice = null;
                if (pokes != null && pokes.size() > 0) {
                    for (Poke poke : pokes) {
                        BigDecimal price = BigDecimal.valueOf(Double.parseDouble(poke.getPrice()));
                        if (lowPokePrice == null) {
                            lowPokePrice = price;
                        } else {
                            if (lowPokePrice.compareTo(price) == 1) {
                                lowPokePrice = price; // take lowest price
                            }
                        }

                        if (highPokePrice == null) {
                            highPokePrice = price;
                        } else {
                            if (highPokePrice.compareTo(price) == -1) {
                                highPokePrice = price; // take highest price
                            }
                        }
                    }
                }

                JSONArray group = new JSONArray();
                group.add(0, curTime);
                group.add(1, temKline.getClosePrice());
                group.add(2, highPokePrice == null ? temKline.getClosePrice() : (temKline.getClosePrice().compareTo(highPokePrice)==-1 ? highPokePrice:temKline.getClosePrice()));
                group.add(3, lowPokePrice == null ? temKline.getClosePrice() : (temKline.getClosePrice().compareTo(lowPokePrice)==1 ? lowPokePrice:temKline.getClosePrice()));
                group.add(4, lastPrice);
                group.add(5, temKline.getVolume());
                array.add(group);
            }
        }else if(temKline!=null && isMin && !resolution.equals("240")){
            // Minute-line compensation
           Long end =  getMinTime(resolution,0);
           Long start =  getMinTime(resolution,-1);

            if (temKline.getTime() > end) {
                // No need to append
                // Set latest price
                JSONArray group  = (JSONArray)array.get(array.size() - 1);
                group.add(4,lastPrice);
                array.set(array.size() - 1,group);
            } else {
                JSONArray group = createNewTempLastKLine(symbol, period, lastPrice, temKline, start,end);
                array.add(group);
            }
        }else if(temKline!=null && isHour){
            // Hour-line compensation
            String pr = "";
            if(resolution.equals("240")){
                pr = "4";
            }else {
                pr = resolution.substring(0,resolution.length()-1);
            }

            Long end =  getHourTime(pr,0);
            Long start =  getHourTime(pr,-1);
            if (temKline.getTime() > end) {
                // No need to append
                // Set latest price
                JSONArray group  = (JSONArray)array.get(array.size() - 1);
                group.add(4,lastPrice);
                array.set(array.size() - 1,group);
            } else {
                JSONArray group = createNewTempLastKLine(symbol, period, lastPrice, temKline, start,end);
                array.add(group);
            }
        }else if(temKline!=null && isDay){
            // Day-line compensation
            String pr = resolution.substring(0,resolution.length()-1);
            Long end =  getDayTime(pr,0);
            Long start =  getDayTime(pr,-1);

            if (temKline.getTime() > end) {
                // No need to append
                // Set latest price
                JSONArray group  = (JSONArray)array.get(array.size() - 1);
                group.add(4,lastPrice);
                array.set(array.size() - 1,group);
            } else {
                JSONArray group = createNewTempLastKLine(symbol, period, lastPrice, temKline, start,end);
                array.add(group);
            }
        }

        return array;
    }

    private JSONArray createNewTempLastKLine(String symbol, String period, BigDecimal lastPrice, KLine temKline, Long start,Long end) {
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
                        lowPokePrice = price; // take lowest price
                    }
                }

                if (highPokePrice == null) {
                    highPokePrice = price;
                } else {
                    if (highPokePrice.compareTo(price) == -1) {
                        highPokePrice = price; // take highest price
                    }
                }
            }
        }
        // Get the highest and lowest 1-minute prices within this period
        JSONArray group = new JSONArray();
        group.add(0, end);
        group.add(1, temKline.getClosePrice());
        group.add(2, highPokePrice == null ? temKline.getClosePrice() : (temKline.getClosePrice().compareTo(highPokePrice)==-1 ? highPokePrice:temKline.getClosePrice()));
        group.add(3, lowPokePrice == null ? temKline.getClosePrice() : (temKline.getClosePrice().compareTo(lowPokePrice)==1 ? lowPokePrice:temKline.getClosePrice()));
        group.add(4, lastPrice);
        group.add(5, temKline.getVolume());
        return group;
    }

    private static Long getMinTime(String resolution,Integer count){
        int pr = Integer.parseInt(resolution);
        Calendar calendar = Calendar.getInstance();
        int m = calendar.get(Calendar.MINUTE);
        calendar.set(Calendar.MINUTE,((m/pr)+count) * pr);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime().getTime();
    }

    private static Long getHourTime(String resolution,Integer count){
        int pr = Integer.parseInt(resolution);
        Calendar calendar = Calendar.getInstance();
        int m = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.set(Calendar.HOUR_OF_DAY,((m/pr)+count) * pr);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return calendar.getTime().getTime();
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
        return calendar.getTime().getTime();
    }

}
