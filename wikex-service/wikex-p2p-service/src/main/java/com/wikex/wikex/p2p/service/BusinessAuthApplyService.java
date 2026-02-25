package com.wikex.wikex.p2p.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.CertifiedBusinessStatus;
import com.wikex.wikex.p2p.entity.BusinessAuthApply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.util.MessageResult;

import java.util.List;


public interface BusinessAuthApplyService extends IService<BusinessAuthApply> {

    List<BusinessAuthApply> findByMemberAndCertifiedBusinessStatus(Long memberId, CertifiedBusinessStatus certifiedBusinessStatus);

    MessageResult detail(Long id);

    Page<BusinessAuthApply> pageApply(Integer pageNo,Integer pageSize, CertifiedBusinessStatus status, String account);

    Integer countAuditing();
}
