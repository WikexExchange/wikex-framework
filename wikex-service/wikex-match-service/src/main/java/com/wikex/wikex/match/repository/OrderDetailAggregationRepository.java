package com.wikex.wikex.match.repository;

import com.wikex.wikex.exchange.entity.OrderDetailAggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderDetailAggregationRepository extends MongoRepository<OrderDetailAggregation,String>{

    List<OrderDetailAggregation> findAllByTimeGreaterThanEqualAndTimeLessThanAndUnit(long var1, long var2, String var3);

}

