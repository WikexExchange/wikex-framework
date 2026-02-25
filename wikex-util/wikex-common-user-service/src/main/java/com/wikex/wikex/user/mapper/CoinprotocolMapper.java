package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.dto.CoinprotocolDTO;
import com.wikex.wikex.user.entity.Coinprotocol;

import java.util.List;


public interface CoinprotocolMapper extends BaseMapper<Coinprotocol> {

    List<CoinprotocolDTO> allCoinprotocolList();
}
