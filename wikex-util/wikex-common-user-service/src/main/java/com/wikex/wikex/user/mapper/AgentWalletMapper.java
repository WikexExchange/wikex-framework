package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.user.entity.AgentWallet;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;


public interface AgentWalletMapper extends BaseMapper<AgentWallet> {

    void increaseBalance(@Param("id") Long id, @Param("amount")BigDecimal amount);

    void decreaseBalance(@Param("id") Long id, @Param("amount")BigDecimal amount);

    void setAgentWalletBalance(@Param("id") Long id, @Param("amount")BigDecimal amount);
}
