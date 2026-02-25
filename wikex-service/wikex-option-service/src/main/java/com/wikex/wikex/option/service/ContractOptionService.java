package com.wikex.wikex.option.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.ContractOptionStatus;
import com.wikex.wikex.option.entity.ContractOption;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.ContractOptionScreen;

import java.math.BigDecimal;
import java.util.List;


public interface ContractOptionService extends IService<ContractOption> {

    List<ContractOption> findBySymbolAndStatus(String symbol, ContractOptionStatus status);

    ContractOption findBySymbolAndOptionNo(String symbol, int perOptionNo);

    void savePresetPrice(String symbol, BigDecimal presetPrice);

    Page<ContractOption> findAll(String symbol, int count);

    ContractOption findOne(Long optionId);

    Page<ContractOption> findAll(ContractOptionScreen screen);
}
