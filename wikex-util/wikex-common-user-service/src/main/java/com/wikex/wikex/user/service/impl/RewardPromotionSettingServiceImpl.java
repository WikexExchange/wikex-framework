package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.constant.PromotionRewardType;
import com.wikex.wikex.user.entity.RewardPromotionSetting;
import com.wikex.wikex.user.mapper.RewardPromotionSettingMapper;
import com.wikex.wikex.user.service.RewardPromotionSettingService;
import org.springframework.stereotype.Service;


@Service
public class RewardPromotionSettingServiceImpl extends ServiceImpl<RewardPromotionSettingMapper, RewardPromotionSetting> implements RewardPromotionSettingService {

    @Override
    public RewardPromotionSetting findByType(PromotionRewardType type) {
        QueryWrapper<RewardPromotionSetting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type",type.getCode());
        return this.getOne(queryWrapper);
    }
}
