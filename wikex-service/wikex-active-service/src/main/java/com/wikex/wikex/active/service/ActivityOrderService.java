package com.wikex.wikex.active.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.active.entity.ActivityOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.exception.WikexRuntimeException;
import com.wikex.wikex.util.MessageResult;

import java.util.List;

/**
 * <p>
 *  
 * </p>
 *
 * @author markchao
 * @since 2021-08-18
 */
public interface ActivityOrderService extends IService<ActivityOrder> {

    IPage<ActivityOrder> finaAllByMemberId(Long memberId, Integer pageNo, Integer pageSize);

    List<ActivityOrder> findAllByActivityIdAndMemberId(Long memberId, Long activityId);

    MessageResult saveActivityOrder(ActivityOrder activityOrder) throws WikexRuntimeException;

    List<ActivityOrder> findAllByActivityId(Long aid);
}
