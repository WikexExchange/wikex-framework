package com.wikex.wikex.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wikex.wikex.dto.CoinDTO;
import com.wikex.wikex.user.dto.ContractDTO;
import com.wikex.wikex.user.entity.Coin;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface CoinMapper extends BaseMapper<Coin> {

    List<CoinDTO> findAllNameAndUnit();

    List<ContractDTO> getContractByProtocol(@Param("protocol")String protocol);

    List<String> findAllName();

    Long getMaxId();
}
