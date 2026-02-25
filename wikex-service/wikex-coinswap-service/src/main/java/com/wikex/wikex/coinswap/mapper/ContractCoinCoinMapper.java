package com.wikex.wikex.coinswap.mapper;

import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


public interface ContractCoinCoinMapper extends BaseMapper<ContractCoinCoin> {

    List<String> getBaseSymbol();

    void increaseOpenFee(@Param("cid") Long cid, @Param("openFee") BigDecimal openFee);

    void increaseCloseFee(@Param("cid") Long cid,@Param("closeFee") BigDecimal closeFee);

    void increaseTotalLoss(@Param("cid") Long cid,@Param("amount") BigDecimal amount);

    void increaseTotalProfit(@Param("cid") Long cid,@Param("amount") BigDecimal amount);
}
