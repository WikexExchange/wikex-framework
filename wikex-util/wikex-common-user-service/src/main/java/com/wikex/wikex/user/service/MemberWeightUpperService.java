package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.swap.vo.RewardSetVo;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWeightUpper;

import java.util.List;


public interface MemberWeightUpperService extends IService<MemberWeightUpper> {

    MemberWeightUpper saveMemberWeightUpper(Member member);

    List<MemberWeightUpper> findAllByUpperIds(String upper);

    MemberWeightUpper findMemberWeightUpperByMemberId(Long memberId);

    RewardSetVo findRewardSetVoById(Long memberId);
}
