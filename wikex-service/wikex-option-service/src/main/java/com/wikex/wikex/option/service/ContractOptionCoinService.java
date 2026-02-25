package com.wikex.wikex.option.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.PageParam;

import java.util.List;


public interface ContractOptionCoinService extends IService<ContractOptionCoin> {

    List<ContractOptionCoin> findAll();

    ContractOptionCoin findBySymbol(String symbol);

    List<ContractOptionCoin> findAllEnabled();

    List<String> getBaseSymbol();

    List<ContractOptionCoin> findAllVisible();

    Page<ContractOptionCoin> findAll(PageParam pageParam);
}
