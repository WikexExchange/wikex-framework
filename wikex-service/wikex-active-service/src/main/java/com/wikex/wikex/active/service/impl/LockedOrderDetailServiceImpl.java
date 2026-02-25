package com.wikex.wikex.active.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.active.entity.LockedOrderDetail;
import com.wikex.wikex.active.mapper.LockedOrderDetailMapper;
import com.wikex.wikex.active.service.LockedOrderDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Locked Order Detail — Service Implementation
 * </p>
 *
 * @author markchao
 * @since 2023-01-09
 */
@Service
public class LockedOrderDetailServiceImpl extends ServiceImpl<LockedOrderDetailMapper, LockedOrderDetail> implements LockedOrderDetailService {

    @Override
    public IPage<LockedOrderDetail> findAllByMiningOrderId(Long miningId, Integer pageNo, Integer pageSize) {
        Page<LockedOrderDetail> page = new Page<>(pageNo, pageSize);
        QueryWrapper<LockedOrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("locked_order_id", miningId);
        queryWrapper.orderByDesc("create_time");
        IPage<LockedOrderDetail> activityOrderPage = this.baseMapper.selectPage(page, queryWrapper);
        return activityOrderPage;
    }
}
