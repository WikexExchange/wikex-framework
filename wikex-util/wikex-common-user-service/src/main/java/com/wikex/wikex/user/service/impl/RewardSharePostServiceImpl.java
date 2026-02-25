package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.RewardSharePost;
import com.wikex.wikex.user.mapper.RewardSharePostMapper;
import com.wikex.wikex.user.service.RewardSharePostService;
import org.springframework.stereotype.Service;

@Service
public class RewardSharePostServiceImpl extends ServiceImpl<RewardSharePostMapper, RewardSharePost>
        implements RewardSharePostService {

    @Override
    public RewardSharePost findOne(Long memberId, Integer year_no, String period, Integer periodNo) {
        QueryWrapper<RewardSharePost> q = new QueryWrapper<>();
        q.eq("member_id", memberId)
                .eq("year_no", year_no)
                .eq("period", period)
                .eq("period_no", periodNo)
                .last("limit 1");
        return this.getOne(q);
    }
}
