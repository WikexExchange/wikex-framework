package com.wikex.wikex.earn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.earn.entity.AutoInvestActivity;
import com.wikex.wikex.earn.entity.LockedSavingsActivity;
import com.wikex.wikex.earn.mapper.AutoInvestActivityMapper;
import com.wikex.wikex.earn.service.AutoInvestActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.earn.vo.ActivityParam;
import com.wikex.wikex.screen.PageParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class AutoInvestActivityServiceImpl extends ServiceImpl<AutoInvestActivityMapper, AutoInvestActivity> implements AutoInvestActivityService {

    @Override
    public Page<AutoInvestActivity> findAll(ActivityParam pageParam) {
        Page<AutoInvestActivity> page = new Page<>(pageParam.getPageNo(),pageParam.getPageSize());
        QueryWrapper<AutoInvestActivity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(pageParam.getStatus()!=null,"status",pageParam.getStatus());
        if(!StringUtils.isEmpty(pageParam.getUnit())){
            queryWrapper.eq("coin_unit",pageParam.getUnit());
        }
        queryWrapper.orderByDesc("create_time");
        return this.page(page,queryWrapper);
    }
}
