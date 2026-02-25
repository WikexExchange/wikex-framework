package com.wikex.wikex.second.service;

import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.Poke;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractMarketService {
    @Autowired
    private MongoTemplate mongoTemplate;

    private Logger logger = LoggerFactory.getLogger(ContractMarketService.class);

    private static String collectionNameKey = "contract_second_";

    public List<KLine> findAllKLine(String symbol, String peroid){
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC,"time"));
        Query query = new Query().with(sort).limit(1000);
        return mongoTemplate.find(query,KLine.class,collectionNameKey+"kline_"+symbol+"_"+peroid);
    }

    public List<KLine> findAllKLine(String symbol,long fromTime,long toTime,String period){

        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.ASC,"time"));
        Query query = new Query(criteria).with(sort);
        List<KLine> kLines = mongoTemplate.find(query,KLine.class,collectionNameKey+"kline_"+symbol.toUpperCase()+"_"+ period);
        return kLines;
    }

    public void saveKLine(String symbol, KLine kLine){
        long timeStamp = findMaxTimestamp(symbol, kLine.getPeriod());
        if(kLine.getTime() == timeStamp){
            return;
        }
       logger.info("Saving K-line (" + symbol + "): " + kLine.getPeriod() + "/" + kLine.getTime() + " ---- maxTime: " + timeStamp);

        mongoTemplate.insert(kLine,collectionNameKey+"kline_"+symbol+"_"+kLine.getPeriod());
    }

    
    public long findMaxTimestamp(String symbol, String period) {
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC,"time"));
        Query query = new Query().with(sort).limit(1);

        List<KLine> result = mongoTemplate.find(query,KLine.class,collectionNameKey+"kline_"+symbol+"_"+period);

        if (result != null && result.size() > 0) {
            return result.get(0).getTime();
        } else {
            return 0;
        }
    }

    
    public List<Poke> findPokeAndRemove(String symbol, String type,String period){
        Query query = new Query();
        List<Poke> pokes = null;
        if(type.equals("kline")) {
            pokes = mongoTemplate.findAllAndRemove(query,Poke.class,collectionNameKey+"poke_"+symbol+"_"+period+"_"+type);
        }else {
            pokes = mongoTemplate.findAllAndRemove(query,Poke.class,collectionNameKey+"poke_"+symbol+"_"+type);
        }

        return pokes;

    }

    
    public List<Poke> findPoke(String symbol, String type,String period){

        List<Poke> pokes = null;
        if(type.equals("kline")) {
            pokes = mongoTemplate.findAll(Poke.class,collectionNameKey+"poke_"+symbol+"_"+period+"_"+type);
        }else {
            pokes = mongoTemplate.findAll(Poke.class,collectionNameKey+"poke_"+symbol+"_"+type);
        }
        return pokes;

    }
}
