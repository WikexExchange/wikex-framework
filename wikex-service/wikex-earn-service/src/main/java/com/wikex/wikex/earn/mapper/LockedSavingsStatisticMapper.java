package com.wikex.wikex.earn.mapper;

import com.wikex.wikex.earn.entity.LockedSavingsStatistic;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;


public interface LockedSavingsStatisticMapper extends BaseMapper<LockedSavingsStatistic> {

    void increaseNum(@Param("id") Long id, @Param("amount")BigDecimal amount);
    void decreaseNumAndIncreaseEarnNum(@Param("id")Long id,@Param("amount") BigDecimal amount, @Param("earnNum")BigDecimal earnNum);
}
