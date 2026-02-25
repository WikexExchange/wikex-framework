package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.constant.WithdrawStatus;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.entity.MemberWallet;
import com.wikex.wikex.user.entity.WithdrawCodeRecord;
import com.wikex.wikex.user.mapper.WithdrawCodeRecordMapper;
import com.wikex.wikex.user.service.MemberTransactionService;
import com.wikex.wikex.user.service.MemberWalletService;
import com.wikex.wikex.user.service.WithdrawCodeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;


@Service
public class WithdrawCodeRecordServiceImpl extends ServiceImpl<WithdrawCodeRecordMapper, WithdrawCodeRecord> implements WithdrawCodeRecordService {

    @Autowired
    private MemberWalletService walletService;
    @Autowired
    private MemberTransactionService transactionService;
    @Override
    public WithdrawCodeRecord findByWithdrawCode(String withdrawCode) {
        LambdaQueryWrapper<WithdrawCodeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WithdrawCodeRecord::getWithdrawCode,withdrawCode);
        return this.getOne(queryWrapper);
    }

    @Override
    public void withdrawSuccess(Long withdrawId, Long memberId) {
        WithdrawCodeRecord record = this.getById(withdrawId);
        if (record != null) {
            
            record.setStatus(WithdrawStatus.SUCCESS.getCode());
            record.setDealTime(new Date());
            record.setRmemberId(memberId);
            this.baseMapper.updateById(record);
            
            MemberWallet wallet = walletService.findByCoinUnitAndMemberId(record.getCoinId(), record.getMemberId());
            if (wallet != null) {
                
                walletService.decreaseFrozen(wallet.getId(),record.getWithdrawAmount());
                
                MemberTransaction transaction = new MemberTransaction();
                transaction.setAmount(record.getWithdrawAmount());
                transaction.setSymbol(wallet.getCoinId());
                transaction.setAddress("");
                transaction.setMemberId(wallet.getMemberId());
                transaction.setType(TransactionType.WITHDRAWCODE_OUT.getCode());
                transaction.setFee(BigDecimal.ZERO);
                transaction.setDiscountFee("0");
                transaction.setRealFee("0");
                transaction.setCreateTime(new Date());
                transactionService.save(transaction);
            }
            
            MemberWallet walletRecharge = walletService.findByCoinUnitAndMemberId(record.getCoinId(), memberId);
            if(walletRecharge != null) {
                walletService.increaseBalance(walletRecharge.getId(),record.getWithdrawAmount());

                MemberTransaction transaction = new MemberTransaction();
                transaction.setAmount(record.getWithdrawAmount());
                transaction.setSymbol(walletRecharge.getCoinId());
                transaction.setAddress("");
                transaction.setMemberId(memberId);
                transaction.setType(TransactionType.WITHDRAWCODE_IN.getCode());
                transaction.setFee(BigDecimal.ZERO);
                transaction.setDiscountFee("0");
                transaction.setRealFee("0");
                transaction.setCreateTime(new Date());
                transactionService.save(transaction);
            }
        }
    }

    @Override
    public IPage<WithdrawCodeRecord> findAllByMemberId(Long memberId, int pageNo, int pageSize) {
        Page<WithdrawCodeRecord> page = new Page<>(pageNo,pageSize);
        LambdaQueryWrapper<WithdrawCodeRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WithdrawCodeRecord::getMemberId,memberId);
        return this.page(page,queryWrapper);
    }
}
