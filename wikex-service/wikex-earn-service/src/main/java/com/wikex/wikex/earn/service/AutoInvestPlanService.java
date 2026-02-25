package com.wikex.wikex.earn.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.earn.entity.AutoInvestActivity;
import com.wikex.wikex.earn.entity.AutoInvestPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.earn.vo.ActivityParam;
import com.wikex.wikex.screen.PageParam;


public interface AutoInvestPlanService extends IService<AutoInvestPlan> {

    IPage<AutoInvestPlan> findAll(Long memberId, PageParam pageParam);

    Page<AutoInvestPlan> findAll4Admin(ActivityParam pageParam);
}
