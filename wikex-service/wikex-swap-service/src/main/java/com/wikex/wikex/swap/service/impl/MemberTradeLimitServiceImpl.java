package com.wikex.wikex.swap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.ContractRewardRecordScreen;
import com.wikex.wikex.swap.entity.MemberTradeLimit;
import com.wikex.wikex.swap.mapper.MemberTradeLimitMapper;
import com.wikex.wikex.swap.service.MemberTradeLimitService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MemberTradeLimitServiceImpl extends ServiceImpl<MemberTradeLimitMapper, MemberTradeLimit> implements MemberTradeLimitService {

    @Override
    public Page<MemberTradeLimit> findAll(ContractRewardRecordScreen screen) {
        Page<MemberTradeLimit> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        QueryWrapper<MemberTradeLimit> queryWrapper = new QueryWrapper<>();
        if(screen.getStartTime() != null) {
            queryWrapper.ge("create_time",screen.getStartTime());
        }
        if(screen.getEndTime() != null) {
            queryWrapper.le("create_time",screen.getEndTime());
        }
        if(screen.getMemberId() != null) {
            queryWrapper.eq("member_id",screen.getMemberId());
        }
        queryWrapper.orderByDesc("create_time");
        return this.page(page,queryWrapper);
    }

    @Override
    public MemberTradeLimit findLimitByMemberIdAndContractId(Long memberId, Long contractId) {
        LambdaQueryWrapper<MemberTradeLimit> query = new LambdaQueryWrapper<>();
        query.eq(MemberTradeLimit::getMemberId,memberId);
        query.eq(MemberTradeLimit::getContractId,contractId);
        query.last("LIMIT 1");
        List<MemberTradeLimit> list = this.list(query);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }
}
