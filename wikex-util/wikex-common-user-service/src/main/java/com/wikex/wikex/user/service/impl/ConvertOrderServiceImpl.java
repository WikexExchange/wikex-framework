package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.ConvertOrderScreen;
import com.wikex.wikex.user.entity.ConvertOrder;
import com.wikex.wikex.user.mapper.ConvertOrderMapper;
import com.wikex.wikex.user.service.ConvertOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;



@Service
public class ConvertOrderServiceImpl extends ServiceImpl<ConvertOrderMapper, ConvertOrder> implements ConvertOrderService {

    public IPage<ConvertOrder> queryByMember(Long memberId, int pageNo, int pageSize) {
        IPage<ConvertOrder> page = new Page<>(pageNo,pageSize);
        QueryWrapper<ConvertOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id",memberId);
        queryWrapper.orderByAsc("create_time");
        return this.page(page,queryWrapper);
    }

    @Override
    public Page<ConvertOrder> findAll(ConvertOrderScreen orderScreen) {
        Page<ConvertOrder> page = new Page<>(orderScreen.getPageNo(),orderScreen.getPageSize());
        QueryWrapper<ConvertOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("create_time");
        if (orderScreen.getMemberId() != null) {
            queryWrapper.eq("member_id",orderScreen.getMemberId());
        }
        String fromUnit = orderScreen.getFromUnit();
        if (!StringUtils.isBlank(fromUnit)) {
            queryWrapper.eq("from_unit",fromUnit);
        }
        String toUnit = orderScreen.getToUnit();
        if (!StringUtils.isBlank(toUnit)) {
            queryWrapper.eq("to_unit",toUnit);
        }
        if(orderScreen.getStartTime() != null) {
            queryWrapper.ge("create_time",orderScreen.getStartTime());
        }
        if(orderScreen.getEndTime() != null) {
            queryWrapper.le("create_time",orderScreen.getEndTime());
        }
        return this.page(page,queryWrapper);
    }
}
