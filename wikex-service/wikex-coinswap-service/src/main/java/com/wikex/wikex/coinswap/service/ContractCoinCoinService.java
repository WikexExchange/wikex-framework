package com.wikex.wikex.coinswap.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;

import java.math.BigDecimal;
import java.util.List;


public interface ContractCoinCoinService extends IService<ContractCoinCoin> {

    List<String> getBaseSymbol();

    ContractCoinCoin findBySymbol(String symbol);

    void increaseTotalOpenFee(Long id, BigDecimal amount);

    void increaseTotalCloseFee(Long id, BigDecimal amount);

    void increaseTotalLoss(Long id, BigDecimal amount);

    void increaseTotalProfit(Long id, BigDecimal amount);

    List<ContractCoinCoin> findAllVisible();

    List<ContractCoinCoin> findAllEnabled();

    Page<ContractCoinCoin> findAll(PageParam pageParam);

    void savePoke(String symbol, BigDecimal price);
}
