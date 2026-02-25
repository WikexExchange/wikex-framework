package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.constant.PromotionRewardType;
import com.wikex.wikex.user.entity.RewardPromotionSetting;


public interface RewardPromotionSettingService extends IService<RewardPromotionSetting> {

    RewardPromotionSetting findByType(PromotionRewardType register);
}
