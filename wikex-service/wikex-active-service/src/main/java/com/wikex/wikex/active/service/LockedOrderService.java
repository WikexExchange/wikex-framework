package com.wikex.wikex.active.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.active.entity.LockedOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author markchao
 * @since 2023-01-09
 */
public interface LockedOrderService extends IService<LockedOrder> {

    IPage<LockedOrder> findAllByMemberIdPage(Long memberId, Integer pageNo, Integer pageSize);

    List<LockedOrder> findAllByMemberIdAndActivityId(Long memberId, Long activityId);

    List<LockedOrder> findAllByLockedStatus(Integer status);
}
