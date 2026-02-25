package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.CoinextScreen;
import com.wikex.wikex.user.entity.Coinext;
import com.wikex.wikex.user.mapper.CoinextMapper;
import com.wikex.wikex.user.service.CoinextService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
public class CoinextServiceImpl extends ServiceImpl<CoinextMapper, Coinext> implements CoinextService {

    @Override
    public Page<Coinext> findAll(CoinextScreen coinextScreen) {

        Page<Coinext> page = new Page<>(coinextScreen.getPageNo(),coinextScreen.getPageSize());
        QueryWrapper<Coinext> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(!StringUtils.isEmpty(coinextScreen.getCoinName()),"coin_name",coinextScreen.getCoinName());
        queryWrapper.eq(!StringUtils.isEmpty(coinextScreen.getExt()),"ext",coinextScreen.getExt());
        return this.page(page,queryWrapper);
    }

    @Override
    public Coinext findFirstByCoinNameAndProtocol(String coinName, Integer protocol) {
        QueryWrapper<Coinext> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coin_name",coinName);
        queryWrapper.eq("protocol",protocol);
        List<Coinext> list = this.list(queryWrapper);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }
}
