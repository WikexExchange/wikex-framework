package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.RewardAsset;

public interface RewardAssetService extends IService<RewardAsset> {
    RewardAsset findByTypePeriodRank(String period, Integer rank);
}
