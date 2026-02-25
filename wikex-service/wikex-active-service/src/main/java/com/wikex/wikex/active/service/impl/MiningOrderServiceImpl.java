package com.wikex.wikex.active.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.active.entity.MiningOrder;
import com.wikex.wikex.active.mapper.MiningOrderMapper;
import com.wikex.wikex.active.service.MiningOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * </p>
 *
 * @author markchao
 * @since 2023-01-09
 */
@Service
public class MiningOrderServiceImpl extends ServiceImpl<MiningOrderMapper, MiningOrder> implements MiningOrderService {

    @Override
    public IPage<MiningOrder> findAllByMemberIdPage(Long memberId, Integer pageNo, Integer pageSize) {
        Page<MiningOrder> page = new Page<>(pageNo,pageSize);
        QueryWrapper<MiningOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.orderByDesc("create_time");
        IPage<MiningOrder> activityOrderPage = this.baseMapper.selectPage(page, queryWrapper);
        return activityOrderPage;
    }

    @Override
    public List<MiningOrder> findAllByMemberIdAndActivityId(Long memberId, Long activityId) {
        LambdaQueryWrapper<MiningOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MiningOrder::getMemberId,memberId);
        queryWrapper.eq(MiningOrder::getActivityId,activityId);
        return this.list(queryWrapper);
    }

    @Override
    public List<MiningOrder> findAllByMiningStatus(Integer status) {
        LambdaQueryWrapper<MiningOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MiningOrder::getMiningStatus,status);
        return this.list(queryWrapper);
    }
}
