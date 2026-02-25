package com.wikex.wikex.kline.service.impl;

import com.wikex.wikex.kline.entity.KLine;
import com.wikex.wikex.kline.entity.Symbol;
import com.wikex.wikex.kline.service.KlineRobotMarketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class BZKlineRobotMarketService implements KlineRobotMarketService {
    @Autowired
    private MongoTemplate mongoTemplate;

    private Logger logger = LoggerFactory.getLogger(BZKlineRobotMarketService.class);


    @Override
    public void saveKLine(String symbol, KLine kLine){
        String period = kLine.getPeriod();
        if(period.equals("60min")){
            period = "1hour";
        }else if(period.equals("1mon")){
            period = "1month";
        }
        kLine.setPeriod(period);
        long timeStamp = findMaxTimestamp(symbol, period);
        if(kLine.getTime() == timeStamp){
            return;
        }

        mongoTemplate.insert(kLine,"exchange_kline_"+symbol+"_"+period);
    }

    /**
     * @param symbol
     * @param period
     * @return
     */
    @Override
    public long findMaxTimestamp(String symbol, String period) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC,"time"));
        Query query = new Query().with(sort).limit(1);
        if(period.equals("60min")){
            period = "1hour";
        }else if(period.equals("1mon")){
            period = "1month";
        }
        List<KLine> result = mongoTemplate.find(query,KLine.class,"exchange_kline_"+symbol+"_"+period);

        if (result != null && result.size() > 0) {
            return result.get(0).getTime();
        } else {
            return 0;
        }
    }

    @Override
    public List<Symbol> findAllSymbol() {
       return mongoTemplate.findAll(Symbol.class,"robot_symbol");
    }

    @Override
    public void addSymbol(Symbol symbol) {
        mongoTemplate.insert(symbol,"robot_symbol");
    }

    @Override
    public void deleteAll(String symbol) {
        mongoTemplate.dropCollection("robot_symbol");
        mongoTemplate.dropCollection("exchange_kline_"+symbol+"_1min");
        mongoTemplate.dropCollection("exchange_kline_"+symbol+"_5min");
        mongoTemplate.dropCollection("exchange_kline_"+symbol+"_10min");
        mongoTemplate.dropCollection("exchange_kline_"+symbol+"_15min");
        mongoTemplate.dropCollection("exchange_kline_"+symbol+"_30min");
        mongoTemplate.dropCollection("exchange_kline_"+symbol+"_1hour");
        mongoTemplate.dropCollection("exchange_kline_"+symbol+"_4hour");
        mongoTemplate.dropCollection("exchange_kline_"+symbol+"_1day");
        mongoTemplate.dropCollection("exchange_kline_"+symbol+"_1week");
        mongoTemplate.dropCollection("exchange_kline_"+symbol+"_1month");
    }
}
