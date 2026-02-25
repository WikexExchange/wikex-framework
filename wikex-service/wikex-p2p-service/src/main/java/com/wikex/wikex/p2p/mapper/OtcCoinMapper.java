package com.wikex.wikex.p2p.mapper;

import com.wikex.wikex.p2p.entity.OtcCoin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;


public interface OtcCoinMapper extends BaseMapper<OtcCoin> {

    List<String> findAllUnits();
}
