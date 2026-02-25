package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.RewardRecordType;
import com.wikex.wikex.user.entity.RewardRecord;
import com.wikex.wikex.user.mapper.RewardRecordMapper;
import com.wikex.wikex.user.service.RewardRecordService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class RewardRecordServiceImpl extends ServiceImpl<RewardRecordMapper, RewardRecord>
        implements RewardRecordService {

    @Override
    public IPage<RewardRecord> queryRewardPromotionPage(Integer pageNo, Integer pageSize, Long memberId) {

        IPage<RewardRecord> page = new Page<>(pageNo, pageSize);
        QueryWrapper<RewardRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", RewardRecordType.CAMPAIGN_EXCHANGE_FEE.getCode());
        queryWrapper.eq("member_id", memberId);
        queryWrapper.orderByDesc("id");
        return this.page(page, queryWrapper);
    }

    @Override
    public BigDecimal getTotalCommissionByMemberId(Long memberId) {
        List<RewardRecord> rewards = this.lambdaQuery()
                .eq(RewardRecord::getMemberId, memberId)
                .eq(RewardRecord::getType, 0) // 0 = Promotion Reward
                .list();

        if (rewards == null || rewards.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return rewards.stream()
                .map(RewardRecord::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalCommissionByMemberId(Long memberId, List<RewardRecordType> types) {
        List<RewardRecord> rewards = this.lambdaQuery()
                .eq(RewardRecord::getMemberId, memberId)
                .in(RewardRecord::getType, types) // 0 = Promotion Reward
                .list();

        if (rewards == null || rewards.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return rewards.stream()
                .map(RewardRecord::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<RewardRecord> getByMemberIdAndType(Long memberId, List<RewardRecordType> types) {
        return this.lambdaQuery()
                .eq(RewardRecord::getMemberId, memberId)
                .in(RewardRecord::getType, types)
                .list();
    }

}
