package com.wikex.wikex.active.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.active.entity.LockedOrder;
import com.wikex.wikex.active.mapper.LockedOrderMapper;
import com.wikex.wikex.active.service.LockedOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class LockedOrderServiceImpl extends ServiceImpl<LockedOrderMapper, LockedOrder> implements LockedOrderService {

    @Override
    public IPage<LockedOrder> findAllByMemberIdPage(Long memberId, Integer pageNo, Integer pageSize) {
        Page<LockedOrder> page = new Page<>(pageNo,pageSize);
        QueryWrapper<LockedOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.orderByDesc("create_time");
        IPage<LockedOrder> orderPage = this.baseMapper.selectPage(page, queryWrapper);
        return orderPage;
    }

    @Override
    public List<LockedOrder> findAllByMemberIdAndActivityId(Long memberId, Long activityId) {
        LambdaQueryWrapper<LockedOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LockedOrder::getMemberId,memberId);
        queryWrapper.eq(LockedOrder::getActivityId,activityId);
        return this.list(queryWrapper);
    }

    @Override
    public List<LockedOrder> findAllByLockedStatus(Integer status) {
        LambdaQueryWrapper<LockedOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LockedOrder::getLockedStatus,status);
        return this.list(queryWrapper);
    }
}
