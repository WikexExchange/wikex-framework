package com.wikex.wikex.earn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.earn.entity.AutoInvestActivity;
import com.wikex.wikex.earn.entity.AutoInvestPlan;
import com.wikex.wikex.earn.mapper.AutoInvestPlanMapper;
import com.wikex.wikex.earn.service.AutoInvestPlanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.earn.vo.ActivityParam;
import com.wikex.wikex.screen.PageParam;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class AutoInvestPlanServiceImpl extends ServiceImpl<AutoInvestPlanMapper, AutoInvestPlan> implements AutoInvestPlanService {

    @Override
    public IPage<AutoInvestPlan> findAll(Long memberId, PageParam pageParam) {

        IPage<AutoInvestPlan> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        QueryWrapper<AutoInvestPlan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.eq("del_flag",0);
        queryWrapper.orderByDesc("create_time");
        return this.page(page,queryWrapper);

    }

    @Override
    public Page<AutoInvestPlan> findAll4Admin(ActivityParam pageParam) {
        Page<AutoInvestPlan> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        QueryWrapper<AutoInvestPlan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("del_flag",0);
        queryWrapper.orderByDesc("create_time");
        return this.page(page,queryWrapper);
    }
}
