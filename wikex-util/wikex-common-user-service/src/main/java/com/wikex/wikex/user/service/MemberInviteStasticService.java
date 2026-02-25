package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.MemberInviteStasticScreen;
import com.wikex.wikex.user.entity.MemberInviteStastic;
import com.wikex.wikex.user.entity.MemberInviteStasticRank;

import java.util.List;


public interface MemberInviteStasticService extends IService<MemberInviteStastic> {

    MemberInviteStastic findByMemberId(Long memberId);

    List<MemberInviteStasticRank> topInviteCountByType(int type, int count);

    List<MemberInviteStastic> topRewardAmount(Integer top);

    List<MemberInviteStastic> topInviteCount(Integer top);

    Page<MemberInviteStastic> queryRankList(MemberInviteStasticScreen screen);
}
