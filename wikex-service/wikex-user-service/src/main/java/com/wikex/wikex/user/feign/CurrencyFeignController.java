package com.wikex.wikex.user.feign;


import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.user.entity.Currency;
import com.wikex.wikex.user.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;



@RestController
@RequestMapping("/currencyFeign")
public class CurrencyFeignController extends BaseController {

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;


    @GetMapping("findAllCurrency")
    public List<Currency> findAllCurrency() {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        String listJson = opsForValue.get(SysConstant.CURRENCY);
        if(!StringUtils.isEmpty(listJson)){
            return JSON.parseArray(listJson,Currency.class);
        }else {
            return currencyService.list();
        }
    }

    @GetMapping("findCurrencyBySymbol")
    public Currency findCurrencyBySymbol(@RequestParam("symbol") String symbol) {
        return currencyService.findCurrencyBySymbol(symbol);
    }


}

