package com.wikex.wikex.coinswap.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.constant.ContractOrderEntrustStatus;
import com.wikex.wikex.constant.ContractOrderPattern;
import com.wikex.wikex.screen.ContractOrderEntrustCoinScreen;
import com.wikex.wikex.coinswap.entity.ContractOrderEntrustCoin;

import java.util.List;


public interface ContractOrderEntrustCoinService extends IService<ContractOrderEntrustCoin> {

    List<ContractOrderEntrustCoin> loadUnMatchOrders(Long id);

    List<ContractOrderEntrustCoin> queryAllEntrustClosingOrdersByContractCoin(Long memberId, Long contractId, ContractOrderDirection direction);

    void updateStatus(Long id, ContractOrderEntrustStatus status);

    IPage<ContractOrderEntrustCoin> queryPageEntrustingOrdersBySymbol(Long memberId, Long contractCoinId, int pageNo, int pageSize);

    IPage<ContractOrderEntrustCoin> queryPageEntrustHistoryOrdersBySymbol(Long memberId, Long contractCoinId, int pageNo, int pageSize);

    long queryEntrustingOrdersCountByContractCoinIdAndPattern(Long memberId, Long contractCoinId, ContractOrderPattern pattern);

    long queryEntrustingOrdersCountByContractCoinId(Long memberId, Long contractCoinId);

    List<ContractOrderEntrustCoin> findCanRewardOrders();

    Page<ContractOrderEntrustCoin> pageQuery(ContractOrderEntrustCoinScreen screen);
}
