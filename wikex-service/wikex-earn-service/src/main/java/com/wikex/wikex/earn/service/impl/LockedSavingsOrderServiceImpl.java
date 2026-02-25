package com.wikex.wikex.earn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.earn.entity.LockedSavingsActivity;
import com.wikex.wikex.earn.entity.LockedSavingsOrder;
import com.wikex.wikex.earn.mapper.LockedSavingsOrderMapper;
import com.wikex.wikex.earn.service.LockedSavingsOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.earn.vo.ActivityParam;
import com.wikex.wikex.screen.PageParam;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class LockedSavingsOrderServiceImpl extends ServiceImpl<LockedSavingsOrderMapper, LockedSavingsOrder> implements LockedSavingsOrderService {

    @Override
    public IPage<LockedSavingsOrder> lockedGoingOrder(Long memberId, PageParam pageParam) {
        IPage<LockedSavingsOrder> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        QueryWrapper<LockedSavingsOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.ge("end_time",new Date());
        queryWrapper.orderByDesc("create_time");
        return this.page(page,queryWrapper);
    }

    @Override
    public IPage<LockedSavingsOrder> lockedDoneOrder(Long memberId, PageParam pageParam) {
        IPage<LockedSavingsOrder> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        QueryWrapper<LockedSavingsOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.eq("status",1);
        queryWrapper.le("end_time",new Date());
        queryWrapper.orderByDesc("create_time");
        return this.page(page,queryWrapper);
    }

    @Override
    public Page<LockedSavingsOrder> findAll(ActivityParam pageParam) {
        Page<LockedSavingsOrder> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        QueryWrapper<LockedSavingsOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return this.page(page,queryWrapper);
    }
}
