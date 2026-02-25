package com.wikex.wikex.option.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.option.entity.ContractOptionOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wikex.wikex.screen.ContractOptionOrderScreen;
import com.wikex.wikex.util.MessageResult;


import java.util.List;


public interface ContractOptionOrderService extends IService<ContractOptionOrder> {

    List<ContractOptionOrder> findByOptionId(Long id);

    Page<ContractOptionOrder> findAll(long id, String symbol, int pageNo, int pageSize);

    List<ContractOptionOrder> findByMemberIdAndOptionId(long id, Long optionId);

    Page<ContractOptionOrder> findAll(ContractOptionOrderScreen screen);

    List<ContractOptionOrder> findByMemberId(Long memberId);

    MessageResult setOptionOrder(Long memberId, Integer optionNo, Short optionNoChange, Short directionChange);
}
