package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.CoinLink;
import com.wikex.wikex.user.mapper.CoinLinkMapper;
import com.wikex.wikex.user.service.CoinLinkService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CoinLinkServiceImpl extends ServiceImpl<CoinLinkMapper, CoinLink> implements CoinLinkService {
    @Override
    public List<CoinLink> findByCoinId(Long coinId) {
        QueryWrapper<CoinLink> qw = new QueryWrapper<>();
        qw.eq("coin_id", coinId);
        return this.list(qw);
    }
}
