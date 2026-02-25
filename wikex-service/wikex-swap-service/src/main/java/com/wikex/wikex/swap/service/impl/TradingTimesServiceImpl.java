package com.wikex.wikex.swap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.swap.entity.TradingTimes;
import com.wikex.wikex.swap.mapper.TradingTimesMapper;
import com.wikex.wikex.swap.service.TradingTimesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class TradingTimesServiceImpl extends ServiceImpl<TradingTimesMapper, TradingTimes> implements TradingTimesService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    @Override
    public List<TradingTimes> findByCoinId(Long contractId) {
        LambdaQueryWrapper<TradingTimes> query = new LambdaQueryWrapper<>();
        query.eq(TradingTimes::getContractCoinId,contractId);
        return this.list(query);
    }

    @Override
    public Page<TradingTimes> findAll(PageParam pageParam) {
        Page<TradingTimes> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        return this.baseMapper.findAll(page);
    }

    
    @Override
    public boolean isTradingTime(Long contractCoinId) {
        
        List<TradingTimes> tradingTimes = this.findByCoinId(contractCoinId);
        if (tradingTimes == null || tradingTimes.isEmpty()) {
            
            return true;
        }

        
        LocalTime now = LocalTime.now();

        
        for (TradingTimes tradingTime : tradingTimes) {
            LocalTime startTime = LocalTime.parse(tradingTime.getStartTime(), TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(tradingTime.getEndTime(), TIME_FORMATTER);
            
            if (now.isAfter(startTime) && now.isBefore(endTime)) {
                return true; 
            }
        }

        
        return false;

    }
}
