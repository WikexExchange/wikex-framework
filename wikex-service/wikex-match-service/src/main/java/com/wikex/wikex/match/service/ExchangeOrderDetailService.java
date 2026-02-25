package com.wikex.wikex.match.service;

import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;

import java.util.List;

public interface ExchangeOrderDetailService {

    public List<ExchangeOrderDetail> findAllByOrderId(String orderId);

    public ExchangeOrderDetail save(ExchangeOrderDetail detail);
}
