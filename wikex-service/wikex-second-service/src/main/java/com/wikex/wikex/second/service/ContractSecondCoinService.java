package com.wikex.wikex.second.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.second.entity.ContractSecondCoin;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;


public interface ContractSecondCoinService extends IService<ContractSecondCoin> {

    List<ContractSecondCoin> findAllEnabled();

    ContractSecondCoin findBySymbol(String symbol);

    List<String> getBaseSymbol();

    List<ContractSecondCoin> findAllVisible();

    void savePoke(String symbol, BigDecimal closePrice);

    Page<ContractSecondCoin> findAll(PageParam pageParam);
}
