package com.wikex.wikex.second.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondCycle;
import com.wikex.wikex.second.mapper.ContractSecondCycleMapper;
import com.wikex.wikex.second.service.ContractSecondCycleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class ContractSecondCycleServiceImpl extends ServiceImpl<ContractSecondCycleMapper, ContractSecondCycle> implements ContractSecondCycleService {

    @Override
    public ContractSecondCycle findOne(Long cycleId) {
        return this.getById(cycleId);
    }

    @Override
    public Page<ContractSecondCycle> findAll(PageParam pageParam) {
        Page<ContractSecondCycle> page = new Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        LambdaQueryWrapper<ContractSecondCycle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ContractSecondCycle::getCreateTime);
        return this.page(page,queryWrapper);
    }
}
