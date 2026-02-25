package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.MemberInviteStasticScreen;
import com.wikex.wikex.user.entity.MemberInviteStastic;
import com.wikex.wikex.user.entity.MemberInviteStasticRank;
import com.wikex.wikex.user.mapper.MemberInviteStasticMapper;
import com.wikex.wikex.user.service.MemberInviteStasticService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class MemberInviteStasticServiceImpl extends ServiceImpl<MemberInviteStasticMapper, MemberInviteStastic> implements MemberInviteStasticService {

    @Override
    public MemberInviteStastic findByMemberId(Long memberId) {
        QueryWrapper<MemberInviteStastic> query = new QueryWrapper<>();
        query.eq("member_id",memberId);
        return this.getOne(query);
    }

    @Override
    public List<MemberInviteStasticRank> topInviteCountByType(int type, int count) {
        return this.baseMapper.topInviteCountByType(type,count);
    }

    @Override
    public List<MemberInviteStastic> topRewardAmount(Integer count) {
        return this.baseMapper.getTopTotalAmount(count);
    }

    @Override
    public List<MemberInviteStastic> topInviteCount(Integer top) {
        return this.baseMapper.getTopInviteCount(top);
    }

    @Override
    public Page<MemberInviteStastic> queryRankList(MemberInviteStasticScreen screen) {

        Page<MemberInviteStastic> page = new Page<>(screen.getPageNo(),screen.getPageSize());
        QueryWrapper<MemberInviteStastic> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotEmpty(screen.getMobilePhone())){
            queryWrapper.eq("user_identify", screen.getMobilePhone());
        }
        if(screen.getRankType().intValue() == 0) {
            queryWrapper.orderByDesc("level_one");
        }
        if(screen.getRankType().intValue() == 1) {
            queryWrapper.orderByDesc("estimated_reward");
        }

        return this.page(page,queryWrapper);
    }
}
