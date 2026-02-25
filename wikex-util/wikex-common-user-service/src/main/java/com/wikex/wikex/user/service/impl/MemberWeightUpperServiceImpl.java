package com.wikex.wikex.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.swap.vo.RewardSetVo;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWeightUpper;
import com.wikex.wikex.user.mapper.MemberWeightUpperMapper;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.user.service.MemberWeightUpperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
public class MemberWeightUpperServiceImpl extends ServiceImpl<MemberWeightUpperMapper, MemberWeightUpper> implements MemberWeightUpperService {

    @Autowired
    private MemberService memberService;

    @Autowired
    private RedisTemplate redisTemplate;

    private String agentKey = "agent-all-rewardset-";

    @Autowired
    private MemberWeightUpperService memberWeightUpperService;

    @Override
    public MemberWeightUpper findMemberWeightUpperByMemberId(Long memberId) {
        QueryWrapper<MemberWeightUpper> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        return this.getOne(queryWrapper);
    }

    @Override
    public MemberWeightUpper saveMemberWeightUpper(Member member) {
        
        MemberWeightUpper memberWeightUpper = this.findMemberWeightUpperByMemberId(member.getId());
        
        if(memberWeightUpper!=null){
            return memberWeightUpper;
        }
        memberWeightUpper = new MemberWeightUpper();
        
        if(member.getInviterId()!=null){
            Member inviter = memberService.getById(member.getInviterId());
            
            MemberWeightUpper upper = this.saveMemberWeightUpper(inviter);
            memberWeightUpper.setFirstMemberId(upper.getFirstMemberId());
            memberWeightUpper.setRate(0);
            memberWeightUpper.setMemberId(member.getId());
            String uppers = upper.getUpper();
            if(uppers==null || "".equals(uppers.trim())){
                uppers = upper.getMemberId().toString();
            }else {
                uppers = uppers+","+upper.getMemberId();
            }
            memberWeightUpper.setUpper(uppers);
        }else {
            
            if("1".equals(member.getSuperPartner())){
                memberWeightUpper.setRate(100);
            }else {
                memberWeightUpper.setRate(0);
            }
            memberWeightUpper.setFirstMemberId(member.getId());
            memberWeightUpper.setMemberId(member.getId());
            memberWeightUpper.setUpper(null);

        }
        this.save(memberWeightUpper);
        return memberWeightUpper;
    }

    @Override
    public List<MemberWeightUpper> findAllByUpperIds(String upper) {
        String[] idss = upper.split(",");
        List<Long> ids = new ArrayList<>();
        for(String id:idss){
            ids.add(Long.parseLong(id));
        }
        return baseMapper.findAllByUpperIds(ids);
    }
    @Override
    public RewardSetVo findRewardSetVoById(Long id){
        String key = agentKey+id;
        RewardSetVo vo = null;
        ValueOperations<String,String> opt = redisTemplate.opsForValue();
        String voJson = opt.get(key);
        if(voJson!=null){
            vo = JSONObject.parseObject(voJson,RewardSetVo.class);
            return vo;
        }
        vo = findRewardSetVoByPid(id,id,true);
        vo.setCanUpdate(false);
        
        String jsonString = JSONObject.toJSONString(vo);
        opt.set(key,jsonString);
        return vo;
    }

    private RewardSetVo findRewardSetVoByPid(Long userId,Long pid,Boolean canUpdate){
        RewardSetVo vo = new RewardSetVo();
        vo.setCanUpdate(canUpdate);
        Member member = memberService.getById(pid);
        if(!"1".equals(member.getSuperPartner())){
            vo.setRate("0%");
            vo.setCanUpdate(false);
        }else {
            MemberWeightUpper upper = memberWeightUpperService.saveMemberWeightUpper(member);
            vo.setRate(upper.getRate()+"%");
        }
        vo.setName(member.getUsername());
        vo.setRealName((member.getRealName()!=null || "".equals(member.getRealName()))?member.getRealName():"Not verified");
        vo.setId(member.getId()+"");
        List<RewardSetVo> children = new ArrayList<>();
        List<Member> members = memberService.findPromotionMember(pid);
        for(Member member1:members){
            children.add(this.findRewardSetVoByPid(userId,member1.getId(),canUpdate));
        }
        vo.setChildren(children);
        return vo;
    }

}
