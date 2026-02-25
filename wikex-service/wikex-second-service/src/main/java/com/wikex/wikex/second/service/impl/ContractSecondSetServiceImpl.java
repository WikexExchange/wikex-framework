package com.wikex.wikex.second.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondSet;
import com.wikex.wikex.second.mapper.ContractSecondSetMapper;
import com.wikex.wikex.second.service.ContractSecondSetService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ContractSecondSetServiceImpl extends ServiceImpl<ContractSecondSetMapper, ContractSecondSet> implements ContractSecondSetService {

    @Override
    public ContractSecondSet findSetByTime(String h) {
        LambdaQueryWrapper<ContractSecondSet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(ContractSecondSet::getStartTime,h);
        queryWrapper.ge(ContractSecondSet::getEndTime,h);
        List<ContractSecondSet> list = this.list(queryWrapper);
        if(list!=null && list.size()>0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public Page<ContractSecondSet> findAll(PageParam pageParam) {
        Page<ContractSecondSet> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        LambdaQueryWrapper<ContractSecondSet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ContractSecondSet::getCreateTime);
        return this.page(page,queryWrapper);
    }


}
