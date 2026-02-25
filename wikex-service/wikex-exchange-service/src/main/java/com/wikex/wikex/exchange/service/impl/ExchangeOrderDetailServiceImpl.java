package com.wikex.wikex.exchange.service.impl;

import com.wikex.wikex.exchange.entity.ExchangeOrderDetail;
import com.wikex.wikex.exchange.repository.ExchangeOrderDetailRepository;
import com.wikex.wikex.exchange.service.ExchangeOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ExchangeOrderDetailServiceImpl implements ExchangeOrderDetailService {
    @Autowired
    private ExchangeOrderDetailRepository orderDetailRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @Override
    public List<ExchangeOrderDetail> findAllByOrderId(String orderId){
        return orderDetailRepository.findAllByOrderId(orderId);
    }
    
    @Override
    public Map<String, ExchangeOrderDetail> findAllByOrderIds(List<String> orderIds){
        if(orderIds == null || orderIds.isEmpty()){
            return new HashMap<>();
        }
        Criteria criteria = Criteria.where("orderId").in(orderIds);
        Query query = new Query(criteria);
        List<ExchangeOrderDetail> allDetails = mongoTemplate.find(query, ExchangeOrderDetail.class,"exchange_order_detail");
        if (allDetails.size() == 0)
            return new HashMap<>();

        Map<String, ExchangeOrderDetail> results = new HashMap<>();
        for(ExchangeOrderDetail order : allDetails)
            results.put(order.getOrderId(), order);
        return results;
    }
    
    @Override
    public ExchangeOrderDetail save(ExchangeOrderDetail detail){
       return orderDetailRepository.save(detail);
    }
}
