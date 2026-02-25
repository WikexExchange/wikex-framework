package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.BooleanEnum;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.screen.MemberWalletScreen;
import com.wikex.wikex.user.dto.MemberWalletDTO;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.mapper.MemberDepositMapper;
import com.wikex.wikex.user.mapper.MemberWalletMapper;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.util.MD5;
import com.wikex.wikex.util.MessageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class MemberWalletServiceImpl extends ServiceImpl<MemberWalletMapper, MemberWallet>
        implements MemberWalletService {

    @Autowired
    private CoinService coinService;
    @Autowired
    private AddressextService addressextService;
    @Autowired
    private CoinprotocolService coinprotocolService;
    @Autowired
    private RechargeService rechargeService;
    @Autowired
    private MemberTransactionService memberTransactionService;

    @Autowired
    private MemberDepositService memberDepositService;

    @Override
    public MemberWallet findByCoinUnitAndMemberId(String coinUnit, Long memberId) {
        QueryWrapper<MemberWallet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId).eq("coin_id", coinUnit);
        return this.getOne(queryWrapper);
    }

    @Override
    public int freezeBalance(Long id, BigDecimal amount) {
        return this.baseMapper.freezeBalance(id, amount);
    }

    @Override
    public List<MemberWallet> findAllByMemberId(Long memberId) {
        QueryWrapper<MemberWallet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", memberId);
        return this.list(queryWrapper);
    }

    @Override
    public int decreaseBalance(Long walletId, BigDecimal amount) {
        return baseMapper.decreaseBalance(walletId, amount);
    }

    @Override
    public int increaseBalance(Long walletId, BigDecimal amount) {
        return baseMapper.increaseBalance(walletId, amount);
    }

    @Override
    public int deductBalance(Long walletId, BigDecimal amount) {
        return baseMapper.decreaseBalance(walletId, amount);
    }

    @Override
    public int decreaseFrozen(Long walletId, BigDecimal amount) {
        return baseMapper.decreaseFrozen(walletId, amount);
    }

    @Override
    public int thawBalance(Long walletId, BigDecimal amount) {
        return baseMapper.thawBalance(walletId, amount);
    }

    @Override
    public Page<MemberWalletDTO> getBalance(MemberWalletScreen screen) {
        IPage<MemberWalletDTO> page = new Page<>(screen.getPageNo(), screen.getPageSize());
        Page<MemberWalletDTO> result = this.baseMapper.getBalance(page, screen);
        return result;
    }

    @Override
    public BigDecimal getBalance(Long memberId, String coinName) {
        MemberWallet wallet = this.findByCoinUnitAndMemberId(coinName, memberId);
        if (wallet != null) {
            return wallet.getBalance();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public int increaseToRelease(Long walletId, BigDecimal amount) {
        return baseMapper.increaseToRelease(walletId, amount);
    }

    @Override
    public int decreaseToRelease(Long walletId, BigDecimal amount) {
        return baseMapper.decreaseToRelease(walletId, amount);
    }

    @Override
    public int increaseFrozen(Long walletId, BigDecimal amount) {
        return baseMapper.increaseFrozen(walletId, amount);
    }

    @Override
    public boolean lockWallet(Long uid, String unit) {
        MemberWallet wallet = findByCoinUnitAndMemberId(unit, uid);
        if (wallet != null && wallet.getIsLock() == BooleanEnum.IS_FALSE) {
            wallet.setIsLock(BooleanEnum.IS_TRUE);
            this.saveOrUpdate(wallet);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean unlockWallet(Long uid, String unit) {
        MemberWallet wallet = findByCoinUnitAndMemberId(unit, uid);
        if (wallet != null && wallet.getIsLock() == BooleanEnum.IS_TRUE) {
            wallet.setIsLock(BooleanEnum.IS_FALSE);
            this.saveOrUpdate(wallet);
            return true;
        } else {
            return false;
        }
    }
}
