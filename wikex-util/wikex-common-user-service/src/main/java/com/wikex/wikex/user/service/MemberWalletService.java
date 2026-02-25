package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.MemberWalletScreen;
import com.wikex.wikex.user.dto.MemberWalletDTO;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.util.MessageResult;

import java.math.BigDecimal;
import java.util.List;


public interface MemberWalletService extends IService<MemberWallet> {

    MemberWallet findByCoinUnitAndMemberId(String coinUnit, Long memberId);

    int freezeBalance(Long id, BigDecimal amount);

    List<MemberWallet> findAllByMemberId(Long memberId);

    int decreaseBalance(Long walletId, BigDecimal amount);

    int increaseBalance(Long walletId, BigDecimal amount);

    int deductBalance(Long walletId, BigDecimal add);

    int decreaseFrozen(Long walletId, BigDecimal amount);

    int thawBalance(Long id, BigDecimal amount);

    Page<MemberWalletDTO> getBalance(MemberWalletScreen screen);

    BigDecimal getBalance(Long memberId, String coinName);

    int increaseToRelease(Long walletId, BigDecimal amount);

    int decreaseToRelease(Long walletId, BigDecimal amount);

    int increaseFrozen(Long walletId, BigDecimal amount);

    boolean lockWallet(Long uid, String unit);

    boolean unlockWallet(Long uid, String unit);
}
