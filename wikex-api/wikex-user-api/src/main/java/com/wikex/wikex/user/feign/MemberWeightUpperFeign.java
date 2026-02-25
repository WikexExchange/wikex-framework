package com.wikex.wikex.user.feign;

import com.wikex.wikex.swap.vo.RewardSetVo;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWeightUpper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


/*****
 * @Author:
 * @Description:
 ****/
@FeignClient(value = "wikex-user",contextId = "memberWeightUpperFeign")
public interface MemberWeightUpperFeign {

    @PostMapping(value = "/memberWeightUpperFeign/findAllByUpperIds")
    List<MemberWeightUpper> findAllByUpperIds(@RequestParam("upper") String upper);

    @PostMapping(value = "/memberWeightUpperFeign/findMemberWeightUpperByMemberId")
    MemberWeightUpper findMemberWeightUpperByMemberId(@RequestParam("memberId") Long memberId);

    @PostMapping(value = "/memberWeightUpperFeign/saveMemberWeightUpper")
    MemberWeightUpper saveMemberWeightUpper(@RequestBody Member member);

    @PostMapping(value = "/memberWeightUpperFeign/modifyMemberWeightUpper")
    Boolean modifyMemberWeightUpper(@RequestBody MemberWeightUpper memberWeightUpper);

    @PostMapping(value = "/memberWeightUpperFeign/findRewardSetVoById")
    RewardSetVo findRewardSetVoById(@RequestParam("memberId") Long memberId);
}
