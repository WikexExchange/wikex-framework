package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.dto.CoinDTO;
import com.wikex.wikex.user.dto.ContractDTO;
import com.wikex.wikex.user.entity.Coin;

import java.util.List;


public interface CoinService extends IService<Coin> {

    Coin findByUnit(String coinUnit);

    Coin findByName(String name);

    List<Coin> findLegalAll();

    IPage findLegalCoinPage(Integer pageNo, Integer pageSize);

    Page<Coin> findAll(Integer pageNo, Integer pageSize);

    List<CoinDTO> findAllNameAndUnit();

    List<Coin> findAllCanWithDraw();

    List<ContractDTO> getContractByProtocol(String protocol);

    List<String> getAllCoinName();

    Long getMaxId();
}
