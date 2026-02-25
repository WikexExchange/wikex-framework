package com.wikex.wikex.option.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.wikex.wikex.option.mapper.ContractOptionCoinMapper;
import com.wikex.wikex.option.service.ContractOptionCoinService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.PageParam;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ContractOptionCoinServiceImpl extends ServiceImpl<ContractOptionCoinMapper, ContractOptionCoin> implements ContractOptionCoinService {

    @Override
    public List<ContractOptionCoin> findAll() {
        return this.list();
    }

    @Override
    public ContractOptionCoin findBySymbol(String symbol) {
        LambdaQueryWrapper<ContractOptionCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractOptionCoin::getSymbol,symbol);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<ContractOptionCoin> findAllEnabled() {
        LambdaQueryWrapper<ContractOptionCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractOptionCoin::getEnable,1);
        queryWrapper.orderByAsc(ContractOptionCoin::getSort);
        return this.list(queryWrapper);
    }

    @Override
    public List<String> getBaseSymbol() {
        return this.baseMapper.getBaseSymbol();
    }

    @Override
    public List<ContractOptionCoin> findAllVisible() {
        LambdaQueryWrapper<ContractOptionCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ContractOptionCoin::getEnable,1);
        queryWrapper.eq(ContractOptionCoin::getVisible,1);
        queryWrapper.orderByAsc(ContractOptionCoin::getSort);
        return this.list(queryWrapper);
    }

    @Override
    public Page<ContractOptionCoin> findAll(PageParam pageParam) {
        Page<ContractOptionCoin> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        LambdaQueryWrapper<ContractOptionCoin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(ContractOptionCoin::getSort);
        return this.page(page,queryWrapper);
    }
}
