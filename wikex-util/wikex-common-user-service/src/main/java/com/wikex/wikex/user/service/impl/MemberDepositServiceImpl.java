package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.MemberDepositScreen;
import com.wikex.wikex.user.entity.MemberDeposit;
import com.wikex.wikex.user.mapper.MemberDepositMapper;
import com.wikex.wikex.user.service.MemberDepositService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberDepositServiceImpl extends ServiceImpl<MemberDepositMapper, MemberDeposit>
        implements MemberDepositService {
    @Override
    public Page<MemberDeposit> findAll(MemberDepositScreen screen) {
        return this.baseMapper.findAll(screen);
    }

    @Override
    public List<MemberDeposit> getDepositStatistics(String dateStr) {
        return this.baseMapper.getDepositStatistics(dateStr);
    }

    @Override
    public MemberDeposit findDeposit(String address, String txid, Integer logIndex) {
        return baseMapper.findDeposit(address, txid, logIndex);
    }

    @Override
    public MemberDeposit findDepositForUpdate(String address, String txid, Integer logIndex) {
        return this.baseMapper.findDepositForUpdate(address, txid, logIndex);
    }

    @Override
    public Page<MemberDeposit> listDeposit(Page<MemberDeposit> page, Integer memberId) {

        return baseMapper.findByMemberId(page, memberId);
    }
}
