package com.wikex.wikex.earn.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.earn.entity.LockedSavingsActivity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.earn.vo.ActivityParam;
import com.wikex.wikex.screen.PageParam;


public interface LockedSavingsActivityService extends IService<LockedSavingsActivity> {

    Page<LockedSavingsActivity> findAll(ActivityParam pageParam);
}
