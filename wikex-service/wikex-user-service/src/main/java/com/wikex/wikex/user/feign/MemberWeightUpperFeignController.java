package com.wikex.wikex.user.feign;

import com.wikex.wikex.controller.BaseController;
import com.wikex.wikex.swap.vo.RewardSetVo;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWeightUpper;
import com.wikex.wikex.user.service.MemberWeightUpperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/memberWeightUpperFeign")
public class MemberWeightUpperFeignController extends BaseController {

    @Autowired
    private MemberWeightUpperService memberWeightUpperService;

    @PostMapping("findAllByUpperIds")
    public List<MemberWeightUpper> findAllByUpperIds(@RequestParam("upper") String upper) {
        List<MemberWeightUpper> list = memberWeightUpperService.findAllByUpperIds(upper);
        return list;
    }

    @PostMapping("findMemberWeightUpperByMemberId")
    public MemberWeightUpper findMemberWeightUpperByMemberId(@RequestParam("memberId") Long memberId) {
        MemberWeightUpper memberWeightUpper = memberWeightUpperService.findMemberWeightUpperByMemberId(memberId);
        return memberWeightUpper;
    }

    @PostMapping(value = "saveMemberWeightUpper")
    public MemberWeightUpper saveMemberWeightUpper(@RequestBody Member member) {
        MemberWeightUpper upper = memberWeightUpperService.saveMemberWeightUpper(member);

        return upper;
    }

    @PostMapping(value = "modifyMemberWeightUpper")
    public Boolean modifyMemberWeightUpper(@RequestBody MemberWeightUpper memberWeightUpper) {
        return memberWeightUpperService.saveOrUpdate(memberWeightUpper);
    }

    @PostMapping("findRewardSetVoById")
    public RewardSetVo findRewardSetVoById(@RequestParam("memberId") Long memberId) {
        return memberWeightUpperService.findRewardSetVoById(memberId);
    }
}
