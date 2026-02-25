package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.Currency;

import java.util.List;

public interface CurrencyService extends IService<Currency> {
    List<Currency> findAll();

    Currency findCurrencyBySymbol(String symbol);
}
