package com.wikex.wikex.swap.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;


public interface ContractCoinService extends IService<ContractCoin> {

    List<String> getBaseSymbol();

    ContractCoin findBySymbol(String symbol);

    void increaseTotalOpenFee(Long id, BigDecimal amount);

    void increaseTotalCloseFee(Long id, BigDecimal amount);

    void increaseTotalLoss(Long id, BigDecimal amount);

    void increaseTotalProfit(Long id, BigDecimal amount);

    List<ContractCoin> findAllVisible();

    List<ContractCoin> findAllEnabled();

    Page<ContractCoin> findAll(PageParam pageParam);

    void savePoke(String symbol, BigDecimal price);
}
