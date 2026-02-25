package com.wikex.wikex.active.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.active.entity.MiningOrder;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author markchao
 * @since 2023-01-09
 */
public interface MiningOrderService extends IService<MiningOrder> {

    IPage<MiningOrder> findAllByMemberIdPage(Long memberId, Integer pageNo, Integer pageSize);

    List<MiningOrder> findAllByMemberIdAndActivityId(Long memberId, Long activityId);

    List<MiningOrder> findAllByMiningStatus(Integer status);
}
