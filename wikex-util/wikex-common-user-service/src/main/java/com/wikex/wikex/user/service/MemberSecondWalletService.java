package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.ContractFinanceScreen;
import com.wikex.wikex.user.entity.MemberSecondWallet;

import java.math.BigDecimal;
import java.util.List;


public interface MemberSecondWalletService extends IService<MemberSecondWallet> {

    List<MemberSecondWallet> findAllByMemberId(Long memberId);

    MemberSecondWallet findByCoinUnitAndMemberId(String unit, Long memberId);

    int increaseBalance(Long walletId, BigDecimal amount);

    int decreaseBalance(Long walletId, BigDecimal amount);

    int decreaseFrozen(Long walletId, BigDecimal amount);

    int thawBalance(Long id, BigDecimal amount);

    int freezeBalance(Long walletId, BigDecimal amount);

    Page<MemberSecondWallet> findAll(ContractFinanceScreen screen);
}
