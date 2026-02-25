package com.wikex.wikex.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.dto.CoinprotocolDTO;
import com.wikex.wikex.user.entity.Coinprotocol;

import java.util.List;


public interface CoinprotocolService extends IService<Coinprotocol> {

    List<CoinprotocolDTO> allCoinprotocolList();

    Coinprotocol findByProtocol(Integer protocol);

    Page<Coinprotocol> findAll(Integer pageNo, Integer pageSize);
}
