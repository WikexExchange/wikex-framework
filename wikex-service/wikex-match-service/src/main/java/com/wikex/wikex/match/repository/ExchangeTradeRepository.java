package com.wikex.wikex.match.repository;

import com.wikex.wikex.pojo.ExchangeTrade;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExchangeTradeRepository extends MongoRepository<ExchangeTrade,String> {
}
