package com.wikex.wikex.exchange.service.impl;

import com.wikex.wikex.exchange.service.ExchangeTradeService;
import com.wikex.wikex.pojo.ExchangeTrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExchangeTradeServiceImpl implements ExchangeTradeService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<ExchangeTrade> findLatest(String symbol, int size){
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC,"time"));
        PageRequest page = PageRequest.of(0,size);
        query.with(page);
        return mongoTemplate.find(query,ExchangeTrade.class,"exchange_trade_"+symbol);
    }
}
