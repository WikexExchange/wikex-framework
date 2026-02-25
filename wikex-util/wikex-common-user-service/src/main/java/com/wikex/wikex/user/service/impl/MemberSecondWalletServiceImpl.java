package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.ContractFinanceScreen;
import com.wikex.wikex.user.entity.MemberSecondWallet;
import com.wikex.wikex.user.mapper.MemberSecondWalletMapper;
import com.wikex.wikex.user.service.MemberSecondWalletService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
public class MemberSecondWalletServiceImpl extends ServiceImpl<MemberSecondWalletMapper, MemberSecondWallet> implements MemberSecondWalletService {

    @Override
    public List<MemberSecondWallet> findAllByMemberId(Long memberId) {
        QueryWrapper<MemberSecondWallet> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MemberSecondWallet::getMemberId,memberId);
        return list(queryWrapper);
    }

    @Override
    public MemberSecondWallet findByCoinUnitAndMemberId(String unit, Long memberId) {
        QueryWrapper<MemberSecondWallet> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MemberSecondWallet::getMemberId,memberId).eq(MemberSecondWallet::getCoinId,unit);
        MemberSecondWallet wallet = baseMapper.selectOne(queryWrapper);
        
        if(wallet==null || wallet.getId()==null){
            wallet = new MemberSecondWallet();
            wallet.setMemberId(memberId);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setCoinId(unit);
            wallet.setFrozenBalance(BigDecimal.ZERO);
            baseMapper.insert(wallet);
        }
        return wallet;
    }

    @Override
    public int increaseBalance(Long walletId, BigDecimal amount) {
        return baseMapper.increaseBalance(walletId, amount);
    }

    @Override
    public int decreaseBalance(Long walletId, BigDecimal amount) {
        return baseMapper.decreaseBalance(walletId, amount);
    }

    @Override
    public int decreaseFrozen(Long walletId, BigDecimal amount) {
        return baseMapper.decreaseFrozen(walletId,amount);
    }

    @Override
    public int thawBalance(Long walletId, BigDecimal amount) {
        return baseMapper.thawBalance(walletId, amount);
    }

    @Override
    public int freezeBalance(Long walletId, BigDecimal amount) {
        return baseMapper.freezeBalance(walletId,amount);
    }

    @Override
    public Page<MemberSecondWallet> findAll(ContractFinanceScreen screen) {

        Page<MemberSecondWallet> page = new Page<>();
        LambdaQueryWrapper<MemberSecondWallet> queryWrapper = new LambdaQueryWrapper<>();
        if (screen.getMemberId() != null) {
            queryWrapper.eq(MemberSecondWallet::getMemberId,screen.getMemberId());
        }
        queryWrapper.orderByDesc(MemberSecondWallet::getId);
        return this.page(page,queryWrapper);
    }
}
