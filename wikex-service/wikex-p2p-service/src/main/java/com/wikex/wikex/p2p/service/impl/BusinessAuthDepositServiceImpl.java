package com.wikex.wikex.p2p.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.p2p.entity.BusinessAuthDeposit;
import com.wikex.wikex.p2p.mapper.BusinessAuthDepositMapper;
import com.wikex.wikex.p2p.service.BusinessAuthDepositService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class BusinessAuthDepositServiceImpl extends ServiceImpl<BusinessAuthDepositMapper, BusinessAuthDeposit> implements BusinessAuthDepositService {

    @Override
    public List<BusinessAuthDeposit> findAllByStatus(CommonStatus normal) {
        QueryWrapper<BusinessAuthDeposit> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status",normal.getCode());
        return this.list(queryWrapper);
    }

    @Override
    public Page<BusinessAuthDeposit> findAll(Integer pageNo,Integer pageSize, CommonStatus status) {
        Page<BusinessAuthDeposit> page = new Page<>(pageNo,pageSize);
        LambdaQueryWrapper<BusinessAuthDeposit> queryWrapper = new LambdaQueryWrapper<>();
        if(status!=null){
            queryWrapper.eq(BusinessAuthDeposit::getStatus,status.getCode());
        }
        return this.page(page,queryWrapper);
    }
}
