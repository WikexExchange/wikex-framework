package com.wikex.wikex.earn.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.earn.entity.LockedSavingsOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.earn.vo.ActivityParam;
import com.wikex.wikex.screen.PageParam;


public interface LockedSavingsOrderService extends IService<LockedSavingsOrder> {

    IPage<LockedSavingsOrder> lockedGoingOrder(Long memberId, PageParam pageParam);

    IPage<LockedSavingsOrder> lockedDoneOrder(Long memberId, PageParam pageParam);

    Page<LockedSavingsOrder> findAll(ActivityParam pageParam);
}
