package com.wikex.wikex.p2p.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.wikex.wikex.p2p.entity.BusinessCancelApply;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;
import java.util.Map;


public interface BusinessCancelApplyService extends IService<BusinessCancelApply> {

    List<BusinessCancelApply> findByMember(Long memberId);

    Page<BusinessCancelApply> findAllCancelApply(Integer pageNo, Integer pageSize, CertifiedBusinessStatus status, String account, Date startDate, Date endDate);

    Map<String, Object> getBusinessOrderStatistics(Long memberId);

    Map<String, Object> getBusinessAppealStatistics(Long memberId);

    Integer countAuditing();
}
