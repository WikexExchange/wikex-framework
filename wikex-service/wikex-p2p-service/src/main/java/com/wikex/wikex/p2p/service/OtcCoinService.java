package com.wikex.wikex.p2p.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.CommonStatus;
import com.wikex.wikex.p2p.entity.OtcCoin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.PageParam;

import java.util.List;


public interface OtcCoinService extends IService<OtcCoin> {

    List<OtcCoin> getNormalCoin();

    Page<OtcCoin> findAllPage(PageParam pageParam);

    List<String> findAllUnits();

    OtcCoin findByUnit(String coinUnit);

    OtcCoin findUnitByUnitAndStatus(String name, CommonStatus normal);
}
