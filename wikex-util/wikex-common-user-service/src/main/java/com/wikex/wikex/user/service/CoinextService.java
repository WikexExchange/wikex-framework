package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.CoinextScreen;
import com.wikex.wikex.user.entity.Coinext;


public interface CoinextService extends IService<Coinext> {

    Page<Coinext> findAll(CoinextScreen coinextScreen);

    Coinext findFirstByCoinNameAndProtocol(String coinName, Integer protocol);

}
