package com.wikex.wikex.option.mapper;

import com.wikex.wikex.option.entity.ContractOptionCoin;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;


public interface ContractOptionCoinMapper extends BaseMapper<ContractOptionCoin> {

    List<String> getBaseSymbol();
}
