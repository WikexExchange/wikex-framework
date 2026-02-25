package com.wikex.wikex.active.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.active.entity.MiningOrderDetail;

/**
 * <p>
 * </p>
 *
 * @author markchao
 * @since 2023-01-09
 */
public interface MiningOrderDetailService extends IService<MiningOrderDetail> {

    IPage<MiningOrderDetail> findAllByMiningOrderId(Long miningId, Integer pageNo, Integer pageSize);
}
