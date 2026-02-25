package com.wikex.wikex.user.service;

import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.constant.RewardRecordType;
import com.wikex.wikex.user.entity.RewardRecord;

public interface RewardRecordService extends IService<RewardRecord> {

    IPage<RewardRecord> queryRewardPromotionPage(Integer pageNo, Integer pageSize, Long memberId);

    BigDecimal getTotalCommissionByMemberId(Long memberId);
    BigDecimal getTotalCommissionByMemberId(Long memberId, List<RewardRecordType> types);
    List<RewardRecord> getByMemberIdAndType(Long memberId, List<RewardRecordType> types);

}
