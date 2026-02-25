package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.user.entity.RedEnvelope;


public interface RedEnvelopeService extends IService<RedEnvelope> {

    RedEnvelope findByEnvelopeNo(String envelopeNo);

    Page<RedEnvelope> findByMember(Long memberId, Integer pageNo, Integer pageSize);
}
