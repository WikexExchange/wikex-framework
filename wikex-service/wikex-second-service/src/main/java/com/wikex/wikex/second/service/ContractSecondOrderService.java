package com.wikex.wikex.second.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wikex.wikex.constant.ContractSecondOrderStatus;
import com.wikex.wikex.screen.ContractSecondOrderScreen;
import com.wikex.wikex.second.entity.ContractSecondOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


public interface ContractSecondOrderService extends IService<ContractSecondOrder> {

    List<ContractSecondOrder> findBySymbolAndStatusAndCloseTime(String symbol, ContractSecondOrderStatus open, Date date);

    void closeOrder(ContractSecondOrder order, BigDecimal closePrice);

    Page<ContractSecondOrder> findAll(Long id, String symbol, int pageNo, int pageSize);

    List<ContractSecondOrder> findOpeningOrders(long id, String symbol);

    List<ContractSecondOrder> findByMemberIdAndSymbolAndStatus(Long id, String symbol, ContractSecondOrderStatus open);

    Integer countOrderByTime(Long id, Date start, Date end);

    Page<ContractSecondOrder> findAll(ContractSecondOrderScreen screen);

    void updatePreClosePrice(Long id, BigDecimal presetPrice);
}
