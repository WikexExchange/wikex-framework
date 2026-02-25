package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.TransactionType;
import com.wikex.wikex.screen.WithdrawScreen;
import com.wikex.wikex.user.entity.*;
import com.wikex.wikex.user.mapper.WithdrawMapper;
import com.wikex.wikex.user.service.*;
import com.wikex.wikex.user.vo.WithdrawVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class WithdrawServiceImpl extends ServiceImpl<WithdrawMapper, Withdraw> implements WithdrawService {

    @Autowired
    private MemberService memberService;
    @Autowired
    private CoinService coinService;
    @Autowired
    private MemberTransactionService memberTransactionService;
    @Autowired
    private MemberFundingWalletService memberFundingWalletService;

    @Override
    public Page<Withdraw> findAllByMemberId(Long id, int pageNo, int pageSize) {
        Page<Withdraw> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<Withdraw> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Withdraw::getMemberId, id);
        queryWrapper.orderByDesc(Withdraw::getId);
        return this.page(page, queryWrapper);
    }

    @Override
    public List<Withdraw> findAllOut(WithdrawScreen withdrawScreen) {
        LambdaQueryWrapper<Withdraw> query = getWithdrawLambdaQueryWrapper(withdrawScreen);
        return this.list(query);
    }

    @Override
    public Page<Withdraw> findAll(WithdrawScreen withdrawScreen) {
        Page<Withdraw> page = new Page<>(withdrawScreen.getPageNo(), withdrawScreen.getPageSize());
        LambdaQueryWrapper<Withdraw> query = getWithdrawLambdaQueryWrapper(withdrawScreen);
        return this.page(page, query);
    }

    @Override
    public Page<Withdraw> joinFind(WithdrawScreen screen) {
        return baseMapper.joinFind(screen);
    }

    @Override
    public List<WithdrawVO> getWithdrawStatistics(String dateStr) {
        return baseMapper.getWithdrawStatistics(dateStr);
    }

    @Override
    public Integer countAuditing() {
        LambdaQueryWrapper<Withdraw> query = new LambdaQueryWrapper<>();
        List<Integer> status = new ArrayList<>();
        status.add(0);
        status.add(1);
        query.in(Withdraw::getStatus, status);
        return this.count(query);
    }

    @Override
    public void withdrawSuccess(Long withdrawId, String txid) {
        Withdraw record = getById(withdrawId);
        if (record != null) {
            record.setHash(txid);
            record.setStatus(2);
            Coin coin = coinService.findByName(record.getCoinName());
            if (coin == null) {
                return;
            }

            MemberFundingWallet fundingWallet = memberFundingWalletService.findByCoinUnitAndMemberId(coin.getUnit(),
                    record.getMemberId().longValue());
            if (fundingWallet != null) {
                memberFundingWalletService.decreaseFrozen(fundingWallet.getId(), record.getMoney());

                MemberTransaction transaction = new MemberTransaction();
                transaction.setAmount(record.getMoney());
                transaction.setSymbol(coin.getUnit());
                transaction.setAddress(record.getAddress());
                transaction.setMemberId(fundingWallet.getMemberId());
                transaction.setType(TransactionType.WITHDRAW.getCode());
                transaction.setFee(record.getFee());
                transaction.setDiscountFee("0");
                transaction.setRealFee(record.getFee() + "");
                transaction.setCreateTime(new Date());
                memberTransactionService.save(transaction);

            }
            this.updateById(record);
        }
    }

    @Override
    public void withdrawFail(Long withdrawId) {
        Withdraw record = getById(withdrawId);
        if (record == null || record.getStatus() != 1) {
            return;
        }
        // MemberWallet wallet = walletService.findByCoinAndMemberId(record.getCoin(),
        // record.getMemberId());
        // if (wallet != null) {
        // wallet.setBalance(wallet.getBalance().add(record.getTotalAmount()));
        // wallet.setFrozenBalance(wallet.getFrozenBalance().subtract(record.getTotalAmount()));
        record.setStatus(3);
        this.updateById(record);
        // }
    }

    @Override
    public List<Withdraw> findWithdrawByStatus(Integer status) {
        LambdaQueryWrapper<Withdraw> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Withdraw::getStatus, status);
        return this.list(queryWrapper);
    }

    @Override
    public void updateWithdrawStatus(Long id, Integer status) {
        LambdaUpdateWrapper<Withdraw> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Withdraw::getStatus, status);
        updateWrapper.eq(Withdraw::getId, id);
        this.update(updateWrapper);
    }

    private LambdaQueryWrapper<Withdraw> getWithdrawLambdaQueryWrapper(WithdrawScreen withdrawScreen) {
        LambdaQueryWrapper<Withdraw> query = new LambdaQueryWrapper<>();
        String email = withdrawScreen.getEmail();
        if (!StringUtils.isBlank(email)) {
            Member byEmail = memberService.findByEmail(email);
            if (byEmail != null) {
                query.eq(Withdraw::getMemberId, byEmail.getId());
            }
        }
        String tel = withdrawScreen.getTel();
        if (!StringUtils.isBlank(tel)) {
            Member byPhone = memberService.findByPhone(tel);
            if (byPhone != null) {
                query.eq(Withdraw::getMemberId, byPhone.getId());
            }
        }
        Long memberId = withdrawScreen.getMemberId();
        if (memberId != null) {
            query.eq(Withdraw::getMemberId, memberId);
        }
        String address = withdrawScreen.getAddress();
        if (!StringUtils.isBlank(address)) {
            query.eq(Withdraw::getAddress, address);
        }

        Integer protocol = withdrawScreen.getProtocol();
        if (protocol != null && protocol > 0) {
            query.eq(Withdraw::getProtocol, protocol);
        }

        String coinname = withdrawScreen.getCoinname();
        if (!StringUtils.isBlank(coinname)) {
            query.eq(Withdraw::getCoinName, coinname);
        }

        Integer status = withdrawScreen.getStatus();
        if (status != null && status > -2) {
            query.eq(Withdraw::getStatus, status);
        }

        String hash = withdrawScreen.getHash();
        if (!StringUtils.isBlank(hash)) {
            query.eq(Withdraw::getHash, hash);
        }

        String startAddTime = withdrawScreen.getStartAddTime();
        String endAddTime = withdrawScreen.getEndAddTime();
        if (!StringUtils.isBlank(startAddTime) && !StringUtils.isBlank(endAddTime)) {
            Date startAddTimeDate = null;
            Date endAddTimeDate = null;
            try {
                startAddTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startAddTime);
                endAddTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endAddTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (startAddTimeDate != null && endAddTimeDate != null) {
                query.between(Withdraw::getAddTime, startAddTimeDate.getTime(), endAddTimeDate.getTime());
            }
        }

        String startProcessTime = withdrawScreen.getStartProcessTime();
        String endProcessTime = withdrawScreen.getEndProcessTime();
        if (!StringUtils.isBlank(startProcessTime) && !StringUtils.isBlank(endProcessTime)) {
            Date startProcessTimeDate = null;
            Date endProcessTimeDate = null;
            try {
                startProcessTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startProcessTime);
                endProcessTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endProcessTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (startProcessTimeDate != null && endProcessTimeDate != null) {
                query.between(Withdraw::getProcessTime, startProcessTimeDate.getTime(), endProcessTimeDate.getTime());
            }
        }
        return query;
    }
}
