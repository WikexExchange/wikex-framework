package com.wikex.wikex.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wikex.wikex.screen.RechargeScreen;
import com.wikex.wikex.user.entity.Recharge;
import com.wikex.wikex.user.mapper.RechargeMapper;
import com.wikex.wikex.user.service.RechargeService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RechargeServiceImpl extends ServiceImpl<RechargeMapper, Recharge> implements RechargeService {


    @Override
    public List<Recharge> findAllOut(RechargeScreen rechargeScreen) {

        LambdaQueryWrapper<Recharge> queryWrapper = getRechargeLambdaQueryWrapper(rechargeScreen);

        return this.list(queryWrapper);
    }

    @Override
    public Page<Recharge> findAll(RechargeScreen rechargeScreen) {
        Page<Recharge> page = new Page<>(rechargeScreen.getPageNo(),rechargeScreen.getPageSize());
        LambdaQueryWrapper<Recharge> query = getRechargeLambdaQueryWrapper(rechargeScreen);
        return this.page(page,query);
    }

    @Override
    public Page<Recharge> findAllByMemberId(Long memberId, int pageNo, int pageSize) {
        Page<Recharge> page = new Page<>(pageNo,pageSize);
        LambdaQueryWrapper<Recharge> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Recharge::getMemberId,memberId);
        return this.page(page,queryWrapper);

    }

    private LambdaQueryWrapper<Recharge> getRechargeLambdaQueryWrapper(RechargeScreen rechargeScreen) {
        LambdaQueryWrapper<Recharge> queryWrapper = new LambdaQueryWrapper<>();
        String address = rechargeScreen.getAddress();
        if (!StringUtils.isBlank(address)) {
            queryWrapper.eq(Recharge::getAddress,address);
        }
        Integer protocol = rechargeScreen.getProtocol();
        if (protocol != null && protocol > 0) {
            queryWrapper.eq(Recharge::getProtocol,protocol);
        }

        String coinName = rechargeScreen.getCoinname();
        if (!StringUtils.isBlank(coinName)) {
            queryWrapper.eq(Recharge::getCoinName,coinName);
        }
        return queryWrapper;
    }
}
