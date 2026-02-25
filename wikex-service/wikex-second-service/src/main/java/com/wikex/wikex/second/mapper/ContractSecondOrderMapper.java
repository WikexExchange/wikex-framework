package com.wikex.wikex.second.mapper;

import com.wikex.wikex.second.entity.ContractSecondOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;


public interface ContractSecondOrderMapper extends BaseMapper<ContractSecondOrder> {

    List<ContractSecondOrder> findOpeningOrders(@Param("memberId") Long memberId, @Param("symbol")String symbol,@Param("closeTime")Date closeTime);
}
