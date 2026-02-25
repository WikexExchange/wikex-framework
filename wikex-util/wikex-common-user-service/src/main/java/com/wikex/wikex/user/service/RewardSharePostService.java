package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.RewardSharePost;

public interface RewardSharePostService extends IService<RewardSharePost> {
    RewardSharePost findOne(Long memberId, Integer year_no, String period, Integer periodIndex);
}
