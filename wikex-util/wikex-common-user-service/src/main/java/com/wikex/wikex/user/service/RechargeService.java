package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.RechargeScreen;
import com.wikex.wikex.user.entity.Recharge;

import java.util.List;


public interface RechargeService extends IService<Recharge> {

    List<Recharge> findAllOut(RechargeScreen screen);

    Page<Recharge> findAll(RechargeScreen rechargeScreen);

    Page<Recharge> findAllByMemberId(Long memberId, int page, int pageSize);
}
