package com.wikex.wikex.option.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOptionOrderDirection;
import com.wikex.wikex.option.engine.ContractOptionCoinMatch;
import com.wikex.wikex.option.engine.ContractOptionCoinMatchFactory;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.wikex.wikex.option.service.ContractMarketService;
import com.wikex.wikex.option.service.ContractOptionCoinService;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ContractOptionTrade;
import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.TradePlateItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ContractOptionCoinService coinService;

    @Autowired
    private ContractOptionCoinMatchFactory contractOptionCoinMatchFactory;

    @Autowired
    private ContractMarketService marketService;

    /**
     * Get supported trading pairs
     * @return
     */
    @ApiOperation(value = "Get supported trading pairs")
    @RequestMapping("symbol")
    public List<ContractOptionCoin> findAllSymbol(){
        List<ContractOptionCoin> coins = coinService.findAllVisible();
        return coins;
    }
    /**
     * Get coin thumbnail market data
     * @return
     */
    @ApiOperation(value = "Get coin thumbnail market data")
    @RequestMapping("symbol-thumb")
    public List<CoinThumb> findSymbolThumb(){
        List<ContractOptionCoin> coins = coinService.findAllVisible();
        List<CoinThumb> thumbs = new ArrayList<>();
        for(ContractOptionCoin coin:coins){
            ContractOptionCoinMatch processor = contractOptionCoinMatchFactory.getContractCoinMatch(coin.getSymbol());
            CoinThumb thumb = processor.getThumb();
            thumb.setZone(0);
            thumbs.add(thumb);
        }
        return thumbs;
    }

    /**
     * Query recent trade records
     * @param symbol trading pair symbol
     * @param size maximum number of records returned
     * @return
     */
    @ApiOperation(value = "Query recent trade records")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
            @ApiImplicitParam(name = "size", value = "Maximum number of records returned"),
    })
    @RequestMapping("latest-trade")
    public List<ContractOptionTrade> latestTrade(String symbol, int size){
        ContractOptionCoinMatch match = contractOptionCoinMatchFactory.getContractCoinMatch(symbol);
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
    public ContractOptionCoin findSymbol(String symbol){
        ContractOptionCoin coin = coinService.findBySymbol(symbol);
        coin.setCurrentTime(Calendar.getInstance().getTimeInMillis());
        return coin;
    }

    @ApiOperation(value = "Get trading pair order book")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("exchange-plate")
    public Map<String,List<TradePlateItem>> findTradePlate(String symbol){
        Map<String, List<TradePlateItem>> result = new HashMap<>();
        ContractOptionCoinMatch match = contractOptionCoinMatchFactory.getContractCoinMatch(symbol);
        if (match == null) {
            return null;
        }
        result.put("bid", match.getTradePlate(ContractOptionOrderDirection.BUY).getItems());
        result.put("ask", match.getTradePlate(ContractOptionOrderDirection.SELL).getItems());

        return result;
    }

    @ApiOperation(value = "Get trading pair order book (mini)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("exchange-plate-mini")
    public Map<String, JSONObject> findTradePlateMini(String symbol){
        Map<String, JSONObject> result = new HashMap<>();
        ContractOptionCoinMatch match = contractOptionCoinMatchFactory.getContractCoinMatch(symbol);
        if (match == null) {
            return null;
        }
        result.put("bid", match.getTradePlate(ContractOptionOrderDirection.BUY).toJSON(20));
        result.put("ask", match.getTradePlate(ContractOptionOrderDirection.SELL).toJSON(20));

        return result;
    }

    @ApiOperation(value = "Get trading pair order book (full)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "symbol", value = "Trading pair symbol"),
    })
    @RequestMapping("exchange-plate-full")
    public Map<String,JSONObject> findTradePlateFull(String symbol){
        Map<String, JSONObject> result = new HashMap<>();
        ContractOptionCoinMatch match = contractOptionCoinMatchFactory.getContractCoinMatch(symbol);
        if (match == null) {
            return null;
        }
        result.put("bid", match.getTradePlate(ContractOptionOrderDirection.BUY).toJSON());
        result.put("ask", match.getTradePlate(ContractOptionOrderDirection.SELL).toJSON());

        return result;
    }

    /**
     * Get historical KLine data
     * @param symbol
     * @param from
     * @param to
     * @param resolution
     * @return
     */
    @ApiOperation(value = "Get historical KLine data")
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
        logger.info("Get historical KLine) symbol: {},  period: {}, from: {}, to: {}, size: {}", symbol, resolution, from, to, list.size());
        JSONArray array = new JSONArray();
        boolean startFlag = false;
        KLine temKline = null;
        for(KLine item:list){
            item.setTime(item.getTime() * 1000);
            // This section handles filtering out initial KLine entries with 0 open/close values
            if(!startFlag && item.getOpenPrice().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }else {
                startFlag = true;
            }
            // If 0 values appear in the middle, adjust accordingly
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
        return array;
    }
}
