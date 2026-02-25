package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.MemberFundingWallet;
import com.wikex.wikex.util.MessageResult;

import java.math.BigDecimal;
import java.util.List;

public interface MemberFundingWalletService extends IService<MemberFundingWallet> {

    List<MemberFundingWallet> findAllByMemberId(Long memberId);

    MemberFundingWallet findByCoinUnitAndMemberId(String unit, Long memberId);

    int increaseBalance(Long walletId, BigDecimal amount);

    int decreaseBalance(Long walletId, BigDecimal amount);

    int increaseFrozen(Long walletId, BigDecimal amount);

    int decreaseFrozen(Long walletId, BigDecimal amount);

    int freezeBalance(Long id, BigDecimal amount);

    int thawBalance(Long walletId, BigDecimal amount);

    MessageResult recharge(String unit, String address, BigDecimal amount, String txid, String fromAddress,
            Long blockHeight);
}
