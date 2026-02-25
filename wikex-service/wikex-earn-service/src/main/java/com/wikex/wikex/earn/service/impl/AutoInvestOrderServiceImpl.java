package com.wikex.wikex.earn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.earn.entity.AutoInvestOrder;
import com.wikex.wikex.earn.mapper.AutoInvestOrderMapper;
import com.wikex.wikex.earn.service.AutoInvestOrderService;
import com.wikex.wikex.screen.PageParam;
import org.springframework.stereotype.Service;


@Service
public class AutoInvestOrderServiceImpl extends ServiceImpl<AutoInvestOrderMapper, AutoInvestOrder> implements AutoInvestOrderService {

    @Override
    public IPage<AutoInvestOrder> findAll(Long memberId, PageParam pageParam) {
        IPage<AutoInvestOrder> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        QueryWrapper<AutoInvestOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.orderByDesc("create_time");
        return this.page(page,queryWrapper);
    }
}
