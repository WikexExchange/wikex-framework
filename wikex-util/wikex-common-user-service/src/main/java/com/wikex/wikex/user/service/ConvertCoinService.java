package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.ConvertCoinScreen;
import com.wikex.wikex.user.entity.ConvertCoin;

import java.util.List;


public interface ConvertCoinService extends IService<ConvertCoin> {

    public boolean save(ConvertCoin convertCoin);

    public ConvertCoin findByCoinUnit(String coinUnit);

    public Page<ConvertCoin> findAll(ConvertCoinScreen convertScreen);

    public List<ConvertCoin> findByStatus(int status);

}
