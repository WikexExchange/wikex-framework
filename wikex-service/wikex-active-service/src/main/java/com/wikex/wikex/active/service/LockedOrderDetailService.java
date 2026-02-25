package com.wikex.wikex.active.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.active.entity.LockedOrderDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * </p>
 *
 * @author markchao
 * @since 2023-01-09
 */
public interface LockedOrderDetailService extends IService<LockedOrderDetail> {

    IPage<LockedOrderDetail> findAllByMiningOrderId(Long miningId, Integer pageNo, Integer pageSize);
}
