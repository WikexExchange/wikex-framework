package com.wikex.wikex.swap.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.ContractRewardRecordScreen;
import com.wikex.wikex.swap.entity.MemberTradeLimit;
import com.baomidou.mybatisplus.extension.service.IService;


public interface MemberTradeLimitService extends IService<MemberTradeLimit> {

    Page<MemberTradeLimit> findAll(ContractRewardRecordScreen screen);

    MemberTradeLimit findLimitByMemberIdAndContractId(Long memberId,Long contractId);
}
