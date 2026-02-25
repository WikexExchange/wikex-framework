package com.wikex.wikex.swap.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.swap.entity.TradingTimes;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface TradingTimesService extends IService<TradingTimes> {

    List<TradingTimes> findByCoinId(Long contractId);

    Page<TradingTimes> findAll(PageParam pageParam);

    boolean isTradingTime(Long contractCoinId);
}
