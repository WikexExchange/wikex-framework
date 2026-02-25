package com.wikex.wikex.p2p.controller;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.p2p.config.CoinExchangeFactory;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.p2p.service.OtcCoinService;
import com.wikex.wikex.util.MessageResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;



@Api(tags = "OTC Coins")
@RestController
@Slf4j
@RequestMapping(value = "/coin")
public class OtcCoinController extends BaseController{

    @Autowired
    private OtcCoinService coinService;
    @Autowired
    private CoinExchangeFactory coins;

    /**
     * Get the coins available for advertisements
     *
     * @return
     */
    @ApiOperation(value = "Get the coins available for advertisements")
    @RequestMapping(value = "all")
    public MessageResult allCoin(@RequestParam(value = "currency", defaultValue = "CNY") String currency){
        List<OtcCoin> list = coinService.getNormalCoin();
        list.stream().forEachOrdered(x ->{
            if(coins.get(x.getUnit(),currency) != null) {
                x.setMarketPrice(coins.get(x.getUnit(),currency).toString());
            }
            x.setBuy_min_amount(x.getBuyMinAmount());
            x.setSell_min_amount(x.getSellMinAmount());
        });
        MessageResult result = success();
        result.setData(list);
        return result;
    }

}
