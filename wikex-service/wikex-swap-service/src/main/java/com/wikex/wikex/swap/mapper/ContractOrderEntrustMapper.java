package com.wikex.wikex.swap.mapper;

import com.wikex.wikex.constant.ContractOrderEntrustStatus;
import com.wikex.wikex.swap.entity.ContractOrderEntrust;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface ContractOrderEntrustMapper extends BaseMapper<ContractOrderEntrust> {

    List<ContractOrderEntrust> loadUnMatchOrders(@Param("contractId") Long contractId);

    void updateStatus(@Param("eid") Long eid, @Param("status") ContractOrderEntrustStatus status);

    void updateReward(@Param("id") Long id, @Param("isReward") Integer isReward);
}
