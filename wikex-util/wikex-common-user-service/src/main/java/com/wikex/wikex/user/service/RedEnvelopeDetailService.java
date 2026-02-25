package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.RedEnvelopeDetail;

import java.util.List;


public interface RedEnvelopeDetailService extends IService<RedEnvelopeDetail> {

    Page<RedEnvelopeDetail> findByEnvelope(Long envelopeId, Integer pageNo, Integer pageSize);

    List<RedEnvelopeDetail> findByEnvelopeIdAndMemberId(Long envelopeId, Long memberId);
}
