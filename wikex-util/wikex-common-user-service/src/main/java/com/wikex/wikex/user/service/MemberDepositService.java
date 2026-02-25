package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.MemberDepositScreen;
import com.wikex.wikex.user.entity.MemberDeposit;

import java.util.List;

public interface MemberDepositService extends IService<MemberDeposit> {

    public Page<MemberDeposit> findAll(MemberDepositScreen scree);

    public List<MemberDeposit> getDepositStatistics(String dateStr);

    public MemberDeposit findDeposit(String address, String txid, Integer logIndex);

    public MemberDeposit findDepositForUpdate(String address, String txid, Integer logIndex);

    public Page<MemberDeposit> listDeposit(Page<MemberDeposit> page, Integer memberId);
}
