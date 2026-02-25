package com.wikex.wikex.earn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wikex.wikex.earn.entity.LockedSavingsStatistic;
import com.wikex.wikex.earn.mapper.LockedSavingsStatisticMapper;
import com.wikex.wikex.earn.service.LockedSavingsStatisticService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Service
public class LockedSavingsStatisticServiceImpl extends ServiceImpl<LockedSavingsStatisticMapper, LockedSavingsStatistic> implements LockedSavingsStatisticService {

    @Override
    public LockedSavingsStatistic findByMemberIdAndCoinSymbol(Long memberId, String coinUnit) {
        LambdaQueryWrapper<LockedSavingsStatistic> query = new LambdaQueryWrapper<>();
        query.eq(LockedSavingsStatistic::getMemberId,memberId);
        query.eq(LockedSavingsStatistic::getCoinSymbol,coinUnit);
        LockedSavingsStatistic statistic = this.getOne(query);
        if(statistic==null){
            statistic = new LockedSavingsStatistic();
            statistic.setMemberId(memberId);
            statistic.setCoinSymbol(coinUnit);
            statistic.setCreateTime(new Date());
            statistic.setEarnNum(BigDecimal.ZERO);
            statistic.setNum(BigDecimal.ZERO);
            this.save(statistic);
        }
        return statistic;
    }

    @Override
    public void increaseNum(Long id, BigDecimal amount) {
        this.baseMapper.increaseNum(id,amount);
    }

    @Override
    public List<LockedSavingsStatistic> findAll(Long memberId) {
        LambdaQueryWrapper<LockedSavingsStatistic> query = new LambdaQueryWrapper<>();
        query.eq(LockedSavingsStatistic::getMemberId,memberId);
        return this.list(query);
    }

    @Override
    public void decreaseNumAndIncreaseEarnNum(Long id, BigDecimal num, BigDecimal earnNum) {
        this.baseMapper.decreaseNumAndIncreaseEarnNum(id,num,earnNum);
    }
}
