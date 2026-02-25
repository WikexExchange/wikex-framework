package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.user.entity.MemberFundingWallet;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface MemberFundingWalletMapper extends BaseMapper<MemberFundingWallet> {

    int increaseBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    int decreaseBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    int increaseFrozen(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    int decreaseFrozen(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    int freezeBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    int thawBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);
}
