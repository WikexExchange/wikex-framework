package com.wikex.wikex.second.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.pojo.*;
import com.wikex.wikex.second.engine.ContractCoinMatch;
import com.wikex.wikex.second.engine.ContractCoinMatchFactory;
import com.wikex.wikex.second.entity.ContractSecondCoin;
import com.wikex.wikex.second.service.ContractMarketService;
import com.wikex.wikex.second.service.ContractSecondCoinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

@Api(tags = "Trading")
@Slf4j
@RestController
public class MarketController {

    private Logger logger = LoggerFactory.getLogger(MarketController.class);
    @Autowired
    private ContractSecondCoinService coinService;

    @Autowired
    private ContractCoinMatchFactory contractCoinMatchFactory;

    @Autowired
    private ContractMarketService marketService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Get supported trading symbols
     * @return
     */
    @ApiOperation(value = "Get supported trading symbols")
    @RequestMapping("symbol")
    public List<ContractSecondCoin> findAllSymbol(){
        List<ContractSecondCoin> coins = coinService.findAllVisible();
        return coins;
    }
    /**
     * Get coin thumbnail market data
     * @return
     */
    @ApiOperation(value = "Get coin thumbnail market data")
    @RequestMapping("symbol-thumb")
    public List<CoinThumb> findSymbolThumb(){
        List<ContractSecondCoin> coins = coinService.findAllVisible();
        List<CoinThumb> thumbs = new ArrayList<>();
        for(ContractSecondCoin coin:coins){
            ContractCoinMatch processor = contractCoinMatchFactory.getContractCoinMatch(coin.getSymbol());
            CoinThumb thumb = processor.getThumb();
            thumbs.add(thumb);
        }
        return thumbs;
    }

    /**
     * Query recent trade records
     * @param symbol Trading pair symbol
     * @param size Maximum number of records to return
     * @return
     */
    @ApiOperation(value = "Query recent trade records")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
            @ApiImplicitParam(name = "size", value = "Maximum number of records to return"),
    })
    @RequestMapping("latest-trade")
    public List<ContractTrade> latestTrade(String symbol, int size){
        ContractCoinMatch match = contractCoinMatchFactory.getContractCoinMatch(symbol);
        if (match == null) {
            return null;
        }
        return match.getLastedTradeList();
    }

    /**
     * Get details of a trading pair
     * @param symbol
     * @return
     */
    @ApiOperation(value = "Get details of a trading pair")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("symbol-info")
    public ContractSecondCoin findSymbol(String symbol){
        ContractSecondCoin coin = coinService.findBySymbol(symbol);
        coin.setCurrentTime(Calendar.getInstance().getTimeInMillis());
        return coin;
    }

    @ApiOperation(value = "Get details of a trading pair")
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

    @ApiOperation(value = "Get details of a trading pair (mini)")
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

    @ApiOperation(value = "Get details of a trading pair (full)")
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
            @ApiImplicitParam(name = "resolution", value = "Time format"),
    })
    @RequestMapping("history")
    public JSONArray findKHistory(String symbol, long from, long to, String resolution){
        String period = "";
        if(resolution.endsWith("H") || resolution.endsWith("h")){
            period = resolution.substring(0,resolution.length()-1) + "hour";
        }
        else if(resolution.endsWith("D") || resolution.endsWith("d")){
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
                period = resolution + "min";
            }
            else {
                period = (val/60) + "hour";
            }
        }
        from = from / 1000;
        to = to / 1000;
        List<KLine> list = marketService.findAllKLine(symbol,from,to,period);
        logger.info("Get historical K-lines) symbol: {},  period: {}, from: {}, to: {}, size: {}", symbol, resolution, from, to, list.size());
        JSONArray array = new JSONArray();
        boolean startFlag = false;
        KLine temKline = null;
        for(KLine item:list){
            item.setTime(item.getTime() * 1000);
            // This part filters out K-lines with 0 open/close at the beginning
            if(!startFlag && item.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }else {
                startFlag = true;
            }
            // If 0 appears in the middle, handle it
            if(item.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                temKline = temKline; // no-op to emphasize logic unchanged
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
        // Current timestamp
        long curTime = System.currentTimeMillis();
        if(temKline!=null) {
            if (temKline.getTime() > curTime) {
                // Do not append
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
                                lowPokePrice = price;// Take the lowest price
                            }
                        }

                        if (highPokePrice == null) {
                            highPokePrice = price;
                        } else {
                            if (highPokePrice.compareTo(price) == -1) {
                                highPokePrice = price;// Take the highest price
                            }
                        }
                    }
                }
                JSONArray group = new JSONArray();
                group.add(0, curTime);
                group.add(1, temKline.getClosePrice());
                group.add(2, highPokePrice == null ? temKline.getClosePrice() : highPokePrice);
                group.add(3, lowPokePrice == null ? temKline.getClosePrice() : lowPokePrice);
                group.add(4, temKline.getClosePrice());
                group.add(5, temKline.getVolume());
                array.add(group);
            }
        }

        return array;
    }
}
