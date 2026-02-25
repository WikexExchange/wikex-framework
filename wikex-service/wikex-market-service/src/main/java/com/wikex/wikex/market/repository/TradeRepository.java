package com.wikex.wikex.market.repository;

import com.wikex.wikex.pojo.ExchangeTrade;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TradeRepository extends MongoRepository<ExchangeTrade,Long>{
}
