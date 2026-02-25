package com.wikex.wikex.user.feign;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.Coin;
import com.wikex.wikex.user.entity.CoinInfo;
import com.wikex.wikex.user.service.CoinInfoService;
import com.wikex.wikex.user.service.CoinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coinInfoFeign")
public class CoinInfoFeignController extends BaseController {
    
    @Autowired
    private CoinInfoService coinInfoService;
    
    @Autowired
    private CoinService coinService;
    
    @GetMapping("/findByCoinId")
    public CoinInfo findByCoinId(@RequestParam("coinId") Long coinId) {
        return coinInfoService.findByCoinId(coinId);
    }
    
    @GetMapping("/findByUnit")
    public CoinInfo findByUnit(@RequestParam("unit") String unit) {
        Coin coin = coinService.findByUnit(unit);
        if (coin == null || coin.getId() == null) {
            return null;
        }
        return coinInfoService.findByCoinId(coin.getId());
    }
    
    @PostMapping("/save")
    public CoinInfo save(@RequestBody CoinInfo coinInfo) {
        coinInfoService.saveOrUpdate(coinInfo);
        return coinInfo;
    }
    
    @PostMapping("/saveOrUpdate")
    public CoinInfo saveOrUpdate(@RequestBody CoinInfo coinInfo) {
        coinInfoService.saveOrUpdate(coinInfo);
        return coinInfo;
    }
}