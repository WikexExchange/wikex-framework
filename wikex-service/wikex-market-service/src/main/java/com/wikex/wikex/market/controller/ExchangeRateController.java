package com.wikex.wikex.market.controller;

import com.wikex.wikex.market.component.CoinExchangeRate;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Api(tags = "Coin Exchange Rate")
@RestController
@RequestMapping("/exchange-rate")
public class ExchangeRateController {
    @Autowired
    private CoinExchangeRate coinExchangeRate;

    @ApiOperation(value = "Get USD coin exchange rate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coin", value = "Coin")
    })
    @RequestMapping("usd/{coin}")
    public MessageResult getUsdExchangeRate(@PathVariable String coin){
        MessageResult mr = new MessageResult(0,"success");
        BigDecimal latestPrice = coinExchangeRate.getUsdRate(coin);
        mr.setData(latestPrice.toString());
        return mr;
    }

    @ApiOperation(value = "Get USDT to CNY exchange rate")
    @RequestMapping("usdtcny")
    public MessageResult getUsdtExchangeRate(){
        MessageResult mr = new MessageResult(0,"success");
        BigDecimal latestPrice = coinExchangeRate.getUsdtCnyRate();
        mr.setData(latestPrice.toString());
        return mr;
    }

    @ApiOperation(value = "Get CNY coin exchange rate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coin", value = "Coin")
    })
    @RequestMapping("cny/{coin}")
    public MessageResult getCnyExchangeRate(@PathVariable String coin){
        MessageResult mr = new MessageResult(0,"success");
        BigDecimal latestPrice = coinExchangeRate.getCnyRate(coin);
        mr.setData(latestPrice.toString());
        return mr;
    }

    @ApiOperation(value = "Get JPY coin exchange rate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coin", value = "Coin")
    })
    @RequestMapping("jpy/{coin}")
    public MessageResult getJpyExchangeRate(@PathVariable String coin){
        MessageResult mr = new MessageResult(0,"success");
        BigDecimal latestPrice = coinExchangeRate.getJpyRate(coin);
        mr.setData(latestPrice.toString());
        return mr;
    }

    @ApiOperation(value = "Get HKD coin exchange rate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "coin", value = "Coin")
    })
    @RequestMapping("hkd/{coin}")
    public MessageResult getHkdExchangeRate(@PathVariable String coin){
        MessageResult mr = new MessageResult(0,"success");
        BigDecimal latestPrice = coinExchangeRate.getHkdRate(coin);
        mr.setData(latestPrice.toString());
        return mr;
    }

    @ApiOperation(value = "Get USD to other currency exchange rate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unit", value = "Currency Unit")
    })
    @RequestMapping("usd-{unit}")
    public MessageResult getUsdCnyRate(@PathVariable String unit){
        MessageResult mr = new MessageResult(0,"success");
        if("CNY".equalsIgnoreCase(unit)) {
            mr.setData(coinExchangeRate.getUsdtCnyRate());
        }
        else if("JPY".equalsIgnoreCase(unit)) {
            mr.setData(coinExchangeRate.getUsdJpyRate());
        }
        else if("HKD".equalsIgnoreCase(unit)) {
            mr.setData(coinExchangeRate.getUsdHkdRate());
        }
        else {
            mr.setData(BigDecimal.ZERO);
        }
        return mr;
    }
}
