package com.wikex.wikex.swap.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.swap.entity.TradingTimes;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;


public interface TradingTimesMapper extends BaseMapper<TradingTimes> {

    Page<TradingTimes> findAll(Page<TradingTimes> page);
}
