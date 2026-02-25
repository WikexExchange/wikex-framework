package com.wikex.wikex.second.mapper;

import com.wikex.wikex.second.entity.ContractSecondCoin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;


public interface ContractSecondCoinMapper extends BaseMapper<ContractSecondCoin> {

    List<String> getBaseSymbol();
}
