package com.wikex.wikex.user.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.swap.vo.RewardSetVo;
import com.wikex.wikex.user.entity.AgentRewardRecord;
import com.wikex.wikex.user.entity.Member;
import com.wikex.wikex.user.entity.MemberWeightUpper;
import com.wikex.wikex.user.mapper.AgentRewardRecordMapper;
import com.wikex.wikex.user.service.AgentRewardRecordService;
import com.wikex.wikex.user.service.MemberService;
import com.wikex.wikex.user.service.MemberWeightUpperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class AgentRewardRecordServiceImpl extends ServiceImpl<AgentRewardRecordMapper, AgentRewardRecord> implements AgentRewardRecordService {

    @Autowired
    private RedisTemplate redisTemplate ;

    private String agentKey = "agent-rewardset-";

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberWeightUpperService memberWeightUpperService;

    @Override
    public void saveAgentRewardRecord(Long fromMemberId,Long memberId,BigDecimal amount,String coinUnit,Integer type,Long orderId) {
        AgentRewardRecord record = new AgentRewardRecord();
        record.setCreateTime(System.currentTimeMillis());
        record.setCoinUnit(coinUnit);
        record.setMemberId(memberId);
        record.setFromMemberId(fromMemberId);
        record.setNum(amount);
        record.setType(type);
        record.setOrderId(orderId);
        this.save(record);
    }

    public RewardSetVo findAllRewardSetVo() {
        RewardSetVo vo = null;
        ValueOperations<String,String> opt = redisTemplate.opsForValue();
        String voJson = opt.get(agentKey);
        if(voJson!=null){
            vo = JSONObject.parseObject(voJson,RewardSetVo.class);
            return vo;
        }
        vo = new RewardSetVo();
        vo.setName("Super Admin");
        vo.setRate("");
        List<Member> members = memberService.findPromotionMember(null);
        List<RewardSetVo> children = new ArrayList<>();
        for(Member member:members){
            children.add(findRewardSetVoByPid(member.getId(),member.getId(),false));
        }
        vo.setChildren(children);
        // put into cache
        String jsonString = JSONObject.toJSONString(vo);
        opt.set(agentKey,jsonString);
        return vo;
    }

    private RewardSetVo findRewardSetVoByPid(Long userId,Long pid,Boolean canUpdate){
        RewardSetVo vo = new RewardSetVo();
        vo.setCanUpdate(canUpdate);
        Member member = memberService.getById(pid);
        if("1".equals(member.getSuperPartner())){
            vo.setCanUpdate(true);
        }
        MemberWeightUpper upper = memberWeightUpperService.saveMemberWeightUpper(member);
        vo.setRate(upper.getRate()+"%");
        vo.setName(member.getUsername());
        vo.setRealName((member.getRealName()!=null || "".equals(member.getRealName()))?member.getRealName():"Not Real-Name Verified");
        vo.setId(member.getId()+"");
        List<RewardSetVo> children = new ArrayList<>();
        List<Member> members = memberService.findPromotionMember(pid);
        for(Member member1:members){
            children.add(this.findRewardSetVoByPid(userId,member1.getId(),false));
        }
        vo.setChildren(children);
        return vo;
    }

    public void clearAllRewardSetVo() {
        ValueOperations<String,String> opt = redisTemplate.opsForValue();
        opt.set(agentKey,null);
    }
}
