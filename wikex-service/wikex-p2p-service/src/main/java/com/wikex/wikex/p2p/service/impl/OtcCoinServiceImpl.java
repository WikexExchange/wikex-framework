package com.wikex.wikex.p2p.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.wikex.wikex.p2p.mapper.OtcCoinMapper;
import com.wikex.wikex.p2p.service.OtcCoinService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.PageParam;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class OtcCoinServiceImpl extends ServiceImpl<OtcCoinMapper, OtcCoin> implements OtcCoinService {

    @Override
    public List<OtcCoin> getNormalCoin() {
        LambdaQueryWrapper<OtcCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OtcCoin::getStatus, CommonStatus.NORMAL.getCode());
        queryWrapper.orderByAsc(OtcCoin::getSort);
        return this.list(queryWrapper);
    }

    @Override
    public Page<OtcCoin> findAllPage(PageParam pageParam) {
        Page<OtcCoin> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        return this.page(page);
    }

    @Override
    public List<String> findAllUnits() {
        return this.baseMapper.findAllUnits();
    }

    @Override
    public OtcCoin findByUnit(String coinUnit) {
        LambdaQueryWrapper<OtcCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OtcCoin::getUnit, coinUnit);
        return this.getOne(queryWrapper);
    }

    @Override
    public OtcCoin findUnitByUnitAndStatus(String name, CommonStatus normal) {
        LambdaQueryWrapper<OtcCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OtcCoin::getUnit, name);
        queryWrapper.eq(OtcCoin::getStatus, normal.getCode());
        return this.getOne(queryWrapper);
    }
}
