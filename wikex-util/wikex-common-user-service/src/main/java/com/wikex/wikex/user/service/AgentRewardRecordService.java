package com.wikex.wikex.user.service;

import com.wikex.wikex.swap.vo.RewardSetVo;

import java.math.BigDecimal;

public interface AgentRewardRecordService {


     void saveAgentRewardRecord(Long fromMemberId,Long memberId,BigDecimal amount,String coinUnit,Integer type,Long orderId);
     RewardSetVo findAllRewardSetVo();

     void clearAllRewardSetVo();
}


