package com.wikex.wikex.exchange.service;

import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;

import java.util.List;
import java.util.Map;

public interface ExchangeOrderDetailService {
    public List<ExchangeOrderDetail> findAllByOrderId(String orderId);

    public Map<String, ExchangeOrderDetail> findAllByOrderIds(List<String> orderIds);

    public ExchangeOrderDetail save(ExchangeOrderDetail detail);
}
