package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.user.entity.Addressext;
import com.wikex.wikex.user.entity.Coinprotocol;
import com.wikex.wikex.user.entity.MemberDeposit;
import com.wikex.wikex.user.entity.MemberFundingWallet;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.Recharge;
import com.wikex.wikex.user.mapper.MemberFundingWalletMapper;
import com.wikex.wikex.user.service.AddressextService;
import com.wikex.wikex.user.service.CoinprotocolService;
import com.wikex.wikex.user.service.MemberDepositService;
import com.wikex.wikex.user.service.MemberFundingWalletService;
import com.wikex.wikex.user.service.MemberTransactionService;
import com.wikex.wikex.user.service.RechargeService;
import com.wikex.wikex.util.MD5;
import com.wikex.wikex.util.MessageResult;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class MemberFundingWalletServiceImpl extends ServiceImpl<MemberFundingWalletMapper, MemberFundingWallet>
        implements MemberFundingWalletService {

    @Autowired
    private AddressextService addressextService;
    @Autowired
    private MemberDepositService memberDepositService;
    @Autowired
    private CoinprotocolService coinprotocolService;
    @Autowired
    private RechargeService rechargeService;
    @Autowired
    private MemberTransactionService memberTransactionService;

    @Override
    public List<MemberFundingWallet> findAllByMemberId(Long memberId) {
        QueryWrapper<MemberFundingWallet> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MemberFundingWallet::getMemberId, memberId);
        return list(queryWrapper);
    }

    @Override
    public MemberFundingWallet findByCoinUnitAndMemberId(String unit, Long memberId) {
        QueryWrapper<MemberFundingWallet> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MemberFundingWallet::getMemberId, memberId).eq(MemberFundingWallet::getCoinId, unit);
        MemberFundingWallet wallet = baseMapper.selectOne(queryWrapper);
        if (wallet == null || wallet.getId() == null) {
            wallet = new MemberFundingWallet();
            wallet.setMemberId(memberId);
            wallet.setCoinId(unit);
            wallet.setBalance(BigDecimal.ZERO);
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
    public int increaseFrozen(Long walletId, BigDecimal amount) {
        return baseMapper.increaseFrozen(walletId, amount);
    }

    @Override
    public int decreaseFrozen(Long walletId, BigDecimal amount) {
        return baseMapper.decreaseFrozen(walletId, amount);
    }

    @Override
    public int freezeBalance(Long id, BigDecimal amount) {
        return baseMapper.freezeBalance(id, amount);
    }

    @Override
    public int thawBalance(Long walletId, BigDecimal amount) {
        return baseMapper.thawBalance(walletId, amount);
    }

    @Override
    public MessageResult recharge(String unit, String address, BigDecimal amount, String txid, String fromAddress,
            Long blockHeight) {
        Addressext addressext = addressextService.findByAddress(address);
        if (addressext == null) {
            return new MessageResult(500, "wallet cannot be null");
        }

        MemberFundingWallet wallet = findByCoinUnitAndMemberId(unit, Long.valueOf(addressext.getMemberId()));
        if (wallet == null) {
            return new MessageResult(500, "wallet cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new MessageResult(500, "amount must large then 0");
        }

        MemberDeposit deposit = new MemberDeposit();
        deposit.setAddress(address);
        deposit.setAmount(amount);
        deposit.setMemberId(wallet.getMemberId());
        deposit.setTxid(txid);
        deposit.setUnit(unit);
        memberDepositService.save(deposit);

        Recharge recharge = new Recharge();
        recharge.setAddress(address);
        recharge.setAddTime(System.currentTimeMillis());
        recharge.setBlock(blockHeight.intValue());
        recharge.setCoinId(0);
        recharge.setCoinName(unit);
        recharge.setMemberId(addressext.getMemberId());
        recharge.setHash(txid);
        recharge.setNConfirms(20);
        recharge.setConfirms(20);
        try {
            if (!StringUtils.isEmpty(txid)) {
                recharge.setMd5(MD5.md5Digest(txid));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        recharge.setMoney(amount);
        recharge.setProtocol(addressext.getCoinProtocol());
        recharge.setSend(fromAddress);
        recharge.setStatus(1);
        recharge.setAgreen(0);
        Coinprotocol byProtocol = coinprotocolService.findByProtocol(addressext.getCoinProtocol());
        if (byProtocol != null) {
            recharge.setProtocolName(byProtocol.getProtocolName());
        }
        rechargeService.save(recharge);

        MemberTransaction transaction = new MemberTransaction();
        transaction.setAmount(amount);
        transaction.setSymbol(unit);
        transaction.setAddress(address);
        transaction.setMemberId(wallet.getMemberId());
        transaction.setType(TransactionType.RECHARGE.getCode());
        transaction.setFee(BigDecimal.ZERO);
        transaction.setDiscountFee("0");
        transaction.setRealFee("0");
        transaction.setCreateTime(new Date());
        memberTransactionService.save(transaction);

        this.increaseBalance(wallet.getId(), amount);

        return new MessageResult(0, "success");
    }
}
