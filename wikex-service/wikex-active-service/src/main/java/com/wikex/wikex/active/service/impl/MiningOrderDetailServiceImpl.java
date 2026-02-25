package com.wikex.wikex.active.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.active.mapper.MiningOrderDetailMapper;
import com.wikex.wikex.active.service.MiningOrderDetailService;
import com.wikex.wikex.active.entity.MiningOrderDetail;
import org.springframework.stereotype.Service;

/**
 * <p>
 * </p>
 *
 * @author markchao
 * @since 2023-01-09
 */
@Service
public class MiningOrderDetailServiceImpl extends ServiceImpl<MiningOrderDetailMapper, MiningOrderDetail> implements MiningOrderDetailService {

    @Override
    public IPage<MiningOrderDetail> findAllByMiningOrderId(Long miningId, Integer pageNo, Integer pageSize) {
        Page<MiningOrderDetail> page = new Page<>(pageNo, pageSize);
        QueryWrapper<MiningOrderDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mining_order_id", miningId);
        queryWrapper.orderByDesc("create_time");
        IPage<MiningOrderDetail> activityOrderPage = this.baseMapper.selectPage(page, queryWrapper);
        return activityOrderPage;
    }
}
