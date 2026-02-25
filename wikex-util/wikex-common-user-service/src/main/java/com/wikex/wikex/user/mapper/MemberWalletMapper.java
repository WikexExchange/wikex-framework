package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.MemberWalletScreen;
import com.wikex.wikex.user.dto.MemberWalletDTO;
import com.wikex.wikex.user.entity.MemberWallet;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;


public interface MemberWalletMapper extends BaseMapper<MemberWallet> {

    @Update("update member_wallet wallet set wallet.balance = wallet.balance - #{amount},wallet.frozen_balance=wallet.frozen_balance + #{amount}  where wallet.id = #{id}  and wallet.balance >= #{amount} ")
    int freezeBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    int decreaseBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    int increaseBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    int decreaseFrozen(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    int thawBalance(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);

    Page<MemberWalletDTO> getBalance(IPage<MemberWalletDTO> page, @Param("screen") MemberWalletScreen screen);

    int increaseToRelease(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);
    int decreaseToRelease(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);
    int increaseFrozen(@Param("walletId") Long walletId, @Param("amount") BigDecimal amount);
}
