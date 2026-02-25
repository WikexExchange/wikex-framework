package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.user.entity.ConvertCoin;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ConvertCoinMapper extends BaseMapper<ConvertCoin> {

    ConvertCoin findByCoinUnit(@Param("coinUnit") String coinUnit);

    List<ConvertCoin> findByStatus(@Param("status")int status);
}
