package com.wikex.wikex.match.service.impl;

import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;
import com.wikex.wikex.match.repository.ExchangeOrderDetailRepository;
import com.wikex.wikex.match.service.ExchangeOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ExchangeOrderDetailServiceImpl implements ExchangeOrderDetailService {
    @Autowired
    private ExchangeOrderDetailRepository orderDetailRepository;

    /**
     * @param orderId
     * @return
     */
    @Override
    public List<ExchangeOrderDetail> findAllByOrderId(String orderId){
        return orderDetailRepository.findAllByOrderId(orderId);
    }
    @Override
    public ExchangeOrderDetail save(ExchangeOrderDetail detail){
       return orderDetailRepository.save(detail);
    }
}
