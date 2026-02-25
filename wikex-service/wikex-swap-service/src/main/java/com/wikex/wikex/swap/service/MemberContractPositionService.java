package com.wikex.wikex.swap.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wikex.wikex.swap.entity.MemberContractPosition;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.List;


public interface MemberContractPositionService extends IService<MemberContractPosition> {

    IPage<MemberContractPosition> queryPageHoldingPositions(Long memberId, Long contractCoinId, int pageNo, int pageSize);

    void setZYZS(Long id, Long positionId, BigDecimal minPrice, BigDecimal maxPrice);

    List<MemberContractPosition> getSetZyZsList(Long contractId, BigDecimal newPrice);

    List<MemberContractPosition> queryHoldingPositions(Long memberId, Long contractCoinId);

    void updateForcePrice(Long memberId, Long id, BigDecimal forcePrice);

    List<Long> queryHoldingPositionMemberIds();

    List<MemberContractPosition> queryAllHoldingPositions(Long memberId);

    List<Long> queryMemberIdsHoldingPositions(Long contractCoinId);

    int freezePosition(Long id, BigDecimal volume);
}
