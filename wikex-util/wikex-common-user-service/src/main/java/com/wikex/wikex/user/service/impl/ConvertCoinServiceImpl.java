package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.ConvertCoinScreen;
import com.wikex.wikex.user.entity.ConvertCoin;
import com.wikex.wikex.user.mapper.ConvertCoinMapper;
import com.wikex.wikex.user.service.ConvertCoinService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ConvertCoinServiceImpl extends ServiceImpl<ConvertCoinMapper, ConvertCoin> implements ConvertCoinService {

    @Override
    public boolean save(ConvertCoin convertCoin) {
        return this.saveOrUpdate(convertCoin);
    }
    @Override
    public ConvertCoin findByCoinUnit(String coinUnit) {
        return this.baseMapper.findByCoinUnit(coinUnit);
    }
    @Override
    public Page<ConvertCoin> findAll(ConvertCoinScreen convertScreen) {
        Page<ConvertCoin> page = new Page<>(convertScreen.getPageNo(),convertScreen.getPageSize());
        QueryWrapper<ConvertCoin> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("create_time");
        return this.page(page,queryWrapper);
    }
    @Override
    public List<ConvertCoin> findByStatus(int status) {
        return this.baseMapper.findByStatus(status);
    }
}
