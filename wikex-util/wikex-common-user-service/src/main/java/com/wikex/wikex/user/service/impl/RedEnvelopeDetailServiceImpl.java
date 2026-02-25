package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.RedEnvelopeDetail;
import com.wikex.wikex.user.mapper.RedEnvelopeDetailMapper;
import com.wikex.wikex.user.service.RedEnvelopeDetailService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RedEnvelopeDetailServiceImpl extends ServiceImpl<RedEnvelopeDetailMapper, RedEnvelopeDetail> implements RedEnvelopeDetailService {

    @Override
    public Page<RedEnvelopeDetail> findByEnvelope(Long envelopeId, Integer pageNo, Integer pageSize) {
        Page<RedEnvelopeDetail> page = new Page<>(pageNo,pageSize);
        LambdaQueryWrapper<RedEnvelopeDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RedEnvelopeDetail::getEnvelopeId,envelopeId);
        queryWrapper.orderByDesc(RedEnvelopeDetail::getCreateTime);
        return this.page(page,queryWrapper);
    }

    @Override
    public List<RedEnvelopeDetail> findByEnvelopeIdAndMemberId(Long envelopeId, Long memberId) {
        LambdaQueryWrapper<RedEnvelopeDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RedEnvelopeDetail::getEnvelopeId,envelopeId);
        queryWrapper.eq(RedEnvelopeDetail::getMemberId,memberId);
        return this.list(queryWrapper);
    }
}
