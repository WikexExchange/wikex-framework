package com.wikex.wikex.exchange.repository;

import com.wikex.wikex.pojo.ExchangeTrade;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExchangeTradeRepository extends MongoRepository<ExchangeTrade,String> {
}
