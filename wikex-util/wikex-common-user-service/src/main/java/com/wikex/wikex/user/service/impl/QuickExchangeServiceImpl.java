package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.QuickExchange;
import com.wikex.wikex.user.mapper.QuickExchangeMapper;
import com.wikex.wikex.user.service.QuickExchangeService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class QuickExchangeServiceImpl extends ServiceImpl<QuickExchangeMapper, QuickExchange> implements QuickExchangeService {

    @Override
    public List<QuickExchange> findAllByMemberId(Long memberId) {
        QueryWrapper<QuickExchange> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        return this.list(queryWrapper);
    }
}
