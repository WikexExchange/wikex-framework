package com.wikex.wikex.earn.service;

import com.wikex.wikex.earn.entity.LockedSavingsStatistic;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;


public interface LockedSavingsStatisticService extends IService<LockedSavingsStatistic> {

    LockedSavingsStatistic findByMemberIdAndCoinSymbol(Long memberId, String coinUnit);

    void increaseNum(Long id, BigDecimal amount);

    List<LockedSavingsStatistic> findAll(Long memberId);

    void decreaseNumAndIncreaseEarnNum(Long id, BigDecimal num, BigDecimal earnNum);
}
