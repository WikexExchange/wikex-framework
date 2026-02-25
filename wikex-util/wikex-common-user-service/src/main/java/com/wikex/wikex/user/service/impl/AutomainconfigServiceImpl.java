package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.user.entity.Automainconfig;
import com.wikex.wikex.user.mapper.AutomainconfigMapper;
import com.wikex.wikex.user.service.AutomainconfigService;
import org.springframework.stereotype.Service;


@Service
public class AutomainconfigServiceImpl extends ServiceImpl<AutomainconfigMapper, Automainconfig> implements AutomainconfigService {

    @Override
    public Page<Automainconfig> findAll(Integer pageNo, Integer pageSize) {
        Page<Automainconfig> page = new Page<>(pageNo,pageSize);
        QueryWrapper<Automainconfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("coin_name");
        return this.page(page,queryWrapper);
    }

    @Override
    public Automainconfig findAutoMainConfigByCoinNameAndProtocol(String coinName, Integer protocol) {
        QueryWrapper<Automainconfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("coin_name",coinName);
        queryWrapper.eq("protocol",protocol);
        return this.getOne(queryWrapper);
    }
}
