package com.wikex.wikex.active.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.active.entity.Activity;
import com.wikex.wikex.active.mapper.ActivityMapper;
import com.wikex.wikex.active.service.ActivityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.PageParam;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * Innovation Lab — Service Implementation
 * </p>
 *
 * @author markchao
 * @since 2021-08-18
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Override
    public IPage<Activity> queryByStep(int pageNo, int pageSize, int step) {
        Page<Activity> page = new Page<>(pageNo, pageSize);
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        if (step != -1) {
            queryWrapper.eq("step", step);
        }
        IPage<Activity> activityIPage = this.baseMapper.selectPage(page, queryWrapper);
        return activityIPage;
    }

    @Override
    public List<Activity> findByTypeAndStep(int type, int step) {
        QueryWrapper<Activity> query = new QueryWrapper<>();
        query.eq("type", type).eq("step", step);
        return this.list(query);
    }

    @Override
    public Page<Activity> findAll(PageParam pageParam) {
        Page<Activity> page = new Page<>(pageParam.getPageNo(), pageParam.getPageSize());
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        return this.page(page, queryWrapper);
    }
}
