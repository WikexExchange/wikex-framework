package com.wikex.wikex.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.SysConstant;
import com.wikex.wikex.user.entity.Currency;
import com.wikex.wikex.user.mapper.CurrencyMapper;
import com.wikex.wikex.user.service.CurrencyService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class CurrencyServiceImpl extends ServiceImpl<CurrencyMapper, Currency> implements CurrencyService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public List<Currency> findAll() {
        String currency = redisTemplate.opsForValue().get(SysConstant.CURRENCY);
        if (!StringUtils.isEmpty(currency)) {
            return JSON.parseArray(currency, Currency.class);
        } else {
            List<Currency> currencyList = this.list();
            redisTemplate.opsForValue().set(SysConstant.CURRENCY, JSON.toJSONString(currencyList), SysConstant.CURRENCY_HALF_HOUR, TimeUnit.SECONDS);
            return currencyList;
        }
    }

    @Override
    public Currency findCurrencyBySymbol(String symbol) {
        LambdaQueryWrapper<Currency> query = new LambdaQueryWrapper<>();
        query.eq(Currency::getSymbol,symbol);
        List<Currency> list = this.list(query);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }
}
