package com.wikex.wikex.p2p.mapper;

import com.wikex.wikex.p2p.entity.BusinessCancelApply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;


public interface BusinessCancelApplyMapper extends BaseMapper<BusinessCancelApply> {

    Map<String, Object> getBusinessStatistics(@Param("memberId") Long memberId);

    Long getBusinessAppealInitiatorIdStatistics(@Param("memberId") Long memberId);

    Long getBusinessAppealAssociateIdStatistics(@Param("memberId") Long memberId);
}
