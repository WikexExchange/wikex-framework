package com.wikex.wikex.coinswap.mapper;

import com.wikex.wikex.coinswap.entity.ContractOrderEntrustCoin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.constant.ContractOrderEntrustStatus;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ContractOrderEntrustCoinMapper extends BaseMapper<ContractOrderEntrustCoin> {
    List<ContractOrderEntrustCoin> loadUnMatchOrders(@Param("contractId") Long contractId);

    void updateStatus(@Param("eid") Long eid, @Param("status") ContractOrderEntrustStatus status);

    void updateReward(@Param("id") Long id, @Param("isReward") Integer isReward);

}
