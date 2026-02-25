package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.RedEnvelope;
import com.wikex.wikex.user.mapper.RedEnvelopeMapper;
import com.wikex.wikex.user.service.RedEnvelopeService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RedEnvelopeServiceImpl extends ServiceImpl<RedEnvelopeMapper, RedEnvelope> implements RedEnvelopeService {

    @Override
    public RedEnvelope findByEnvelopeNo(String envelopeNo) {
        LambdaQueryWrapper<RedEnvelope> query = new LambdaQueryWrapper<>();
        query.eq(RedEnvelope::getEnvelopeNo,envelopeNo);
        List<RedEnvelope> list = this.list(query);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public Page<RedEnvelope> findByMember(Long memberId, Integer pageNo, Integer pageSize) {

        Page<RedEnvelope> page = new Page<>(pageNo,pageSize);
        LambdaQueryWrapper<RedEnvelope> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RedEnvelope::getMemberId,memberId);
        queryWrapper.orderByDesc(RedEnvelope::getCreateTime);
        return this.page(page, queryWrapper);

    }
}
