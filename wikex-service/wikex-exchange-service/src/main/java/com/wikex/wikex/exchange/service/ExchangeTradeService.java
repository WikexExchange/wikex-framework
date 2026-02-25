package com.wikex.wikex.exchange.service;

import com.wikex.wikex.pojo.ExchangeTrade;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ExchangeTradeService {


    public List<ExchangeTrade> findLatest(String symbol, int size);
}
