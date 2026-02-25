package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.RewardAsset;
import com.wikex.wikex.user.mapper.RewardAssetMapper;
import com.wikex.wikex.user.service.RewardAssetService;
import org.springframework.stereotype.Service;

@Service
public class RewardAssetServiceImpl extends ServiceImpl<RewardAssetMapper, RewardAsset> implements RewardAssetService {

    @Override
    public RewardAsset findByTypePeriodRank(String period, Integer rank) {
        QueryWrapper<RewardAsset> q = new QueryWrapper<>();
        q.eq("period", period)
                .eq("rank", rank)
                .last("limit 1");
        return this.getOne(q);
    }
}