package com.wikex.wikex.active.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.active.entity.Activity;
import com.wikex.wikex.screen.PageParam;


import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author markchao
 * @since 2021-08-18
 */
public interface ActivityService extends IService<Activity> {

    IPage<Activity> queryByStep(int pageNo, int pageSize, int step);

    List<Activity> findByTypeAndStep(int type, int step);

    Page<Activity> findAll(PageParam pageParam);
}
