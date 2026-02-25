package com.wikex.wikex.swap.mapper;

import com.wikex.wikex.swap.entity.ContractCoin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


public interface ContractCoinMapper extends BaseMapper<ContractCoin> {
    List<String> getBaseSymbol();

    void increaseOpenFee(@Param("cid") Long cid,@Param("openFee") BigDecimal openFee);

    void increaseCloseFee(@Param("cid") Long cid,@Param("closeFee") BigDecimal closeFee);

    void increaseTotalLoss(@Param("cid") Long cid,@Param("amount") BigDecimal amount);

    void increaseTotalProfit(@Param("cid") Long cid,@Param("amount") BigDecimal amount);
}
