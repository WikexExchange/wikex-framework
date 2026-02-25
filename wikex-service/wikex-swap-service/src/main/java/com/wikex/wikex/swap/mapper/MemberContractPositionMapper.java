package com.wikex.wikex.swap.mapper;

import com.wikex.wikex.swap.entity.MemberContractPosition;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;


public interface MemberContractPositionMapper extends BaseMapper<MemberContractPosition> {

    List<MemberContractPosition> getSetZyZsList(@Param("contractId") Long contractId, @Param("newPrice")BigDecimal newPrice);

    List<Long> queryHoldingPositionMemberIds();

    List<MemberContractPosition> queryAllHoldingPositions(@Param("memberId")Long memberId);

    int freezePosition(@Param("id")Long id, @Param("volume")BigDecimal volume);
}
