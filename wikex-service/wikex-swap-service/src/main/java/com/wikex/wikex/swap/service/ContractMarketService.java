package com.wikex.wikex.swap.service;


import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.Poke;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class ContractMarketService  {
    @Autowired
    private MongoTemplate mongoTemplate;

    public List<KLine> findAllKLine(String symbol, String peroid){
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC,"time"));
        Query query = new Query().with(sort).limit(1000);
        return mongoTemplate.find(query,KLine.class,"contract_kline_"+symbol+"_"+peroid);
    }

    public List<KLine> findAllKLine(String symbol,long fromTime,long toTime,String period){
        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC,"time"));
        Query query = new Query(criteria).with(sort);
        List<KLine> kLines = mongoTemplate.find(query,KLine.class,"contract_kline_"+symbol.toUpperCase()+"_"+ period);
        return kLines;
    }

    public void saveKLine(String symbol, KLine kLine){
        long timeStamp = findMaxTimestamp(symbol, kLine.getPeriod());
        if(kLine.getTime() == timeStamp){
            return;
        }
        
        mongoTemplate.insert(kLine,"contract_kline_"+symbol+"_"+kLine.getPeriod());
    }

    public void updateKLine(String symbol, KLine kLine){
        Criteria criteria = Criteria.where("time").is(kLine.getTime());
        Query query = new Query();
        query.addCriteria(criteria);
        
        Update update = new Update().set("highestPrice", kLine.getHighestPrice()).set("lowestPrice",kLine.getLowestPrice());
        mongoTemplate.updateFirst(query,update,"contract_kline_"+symbol+"_"+kLine.getPeriod());
    }

    
    public long findMaxTimestamp(String symbol, String period) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC,"time"));
        Query query = new Query().with(sort).limit(1);

        List<KLine> result = mongoTemplate.find(query,KLine.class,"contract_kline_"+symbol+"_"+period);

        if (result != null && result.size() > 0) {
            return result.get(0).getTime();
        } else {
            return 0;
        }
    }


    
    public BigDecimal findMaxPrice(String symbol, String period,Long start,Long end) {
        Criteria criteria = Criteria.where("time").gte(start).andOperator(Criteria.where("time").lte(end));
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC,"highestPrice"));
        Query query = new Query(criteria).with(sort);
        List<KLine> result = mongoTemplate.find(query,KLine.class,"contract_kline_"+symbol+"_"+period);
        if (result != null && result.size() > 0) {
            return result.get(0).getHighestPrice();
        } else {
            return null;
        }
    }


    
    public BigDecimal findMinPrice(String symbol, String period,Long start,Long end) {
        Criteria criteria = Criteria.where("time").gte(start).andOperator(Criteria.where("time").lte(end));
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC,"highestPrice"));
        Query query = new Query(criteria).with(sort);
        List<KLine> result = mongoTemplate.find(query,KLine.class,"contract_kline_"+symbol+"_"+period);
        if (result != null && result.size() > 0) {
            return result.get(0).getLowestPrice();
        } else {
            return null;
        }
    }
    
    public List<Poke> findPokeAndRemove(String symbol, String type,String period){
        Query query = new Query();
        List<Poke> pokes = null;
        if(type.equals("kline")) {
            pokes = mongoTemplate.findAllAndRemove(query,Poke.class,"contract_poke_"+symbol+"_"+period+"_"+type);
        }else {
            pokes = mongoTemplate.findAllAndRemove(query,Poke.class,"contract_poke_"+symbol+"_"+type);
        }
        return pokes;

    }

    
    public List<Poke> findPoke(String symbol, String type,String period){
        List<Poke> pokes = null;
        if(type.equals("kline")) {
            pokes = mongoTemplate.findAll(Poke.class,"contract_poke_"+symbol+"_"+period+"_"+type);
        }else {
            pokes = mongoTemplate.findAll(Poke.class,"contract_poke_"+symbol+"_"+type);
        }
        return pokes;
    }

    public List<KLine> findKLineByTime(String symbol, long time, String period) {
        Criteria criteria = Criteria.where("time").is(time);
        Query query = new Query();
        query.addCriteria(criteria);
        return mongoTemplate.find(query,KLine.class,"contract_kline_"+symbol+"_"+period);
    }
}
