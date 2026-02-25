package com.wikex.wikex.swap.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.constant.ContractOrderEntrustStatus;
import com.wikex.wikex.constant.ContractOrderPattern;
import com.wikex.wikex.screen.ContractOrderEntrustScreen;
import com.wikex.wikex.screen.PageParam;
import com.wikex.wikex.swap.entity.ContractOrderEntrust;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


public interface ContractOrderEntrustService extends IService<ContractOrderEntrust> {

    List<ContractOrderEntrust> loadUnMatchOrders(Long id);

    List<ContractOrderEntrust> queryAllEntrustClosingOrdersByContractCoin(Long memberId, Long contractId, ContractOrderDirection direction);

    List<ContractOrderEntrust> queryAllClosingOrdersByPositionId(Long memberId,Long positionId,Long contractId, ContractOrderDirection direction);

    void updateStatus(Long id, ContractOrderEntrustStatus status);

    IPage<ContractOrderEntrust> queryPageEntrustingOrdersBySymbol(Long memberId, Long contractCoinId, int pageNo, int pageSize);

    IPage<ContractOrderEntrust> queryPageEntrustHistoryOrdersBySymbol(Long memberId, Long contractCoinId, int pageNo, int pageSize);

    long queryEntrustingOrdersCountByContractCoinIdAndPattern(Long memberId, Long contractCoinId, ContractOrderPattern pattern);

    long queryEntrustingOrdersCountByContractCoinId(Long memberId, Long contractCoinId);

    List<ContractOrderEntrust> findCanRewardOrders();

    Page<ContractOrderEntrust> pageQuery(ContractOrderEntrustScreen screen);

    Page<ContractOrderEntrust> findAll4Agent(Long memberId, PageParam pageParam, ContractOrderEntrustScreen screen);

    void sendReward();

    List<ContractOrderEntrust> queryEntrustingOrdersByContractCoinId(Long memberId, Long contractCoinId);
}
