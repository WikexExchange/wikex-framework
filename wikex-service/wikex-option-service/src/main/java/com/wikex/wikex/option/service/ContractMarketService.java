package com.wikex.wikex.option.service;

import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.PresetPrice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class ContractMarketService {
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

    
    public PresetPrice findPresetPrice(String symbol, String type){
        Query query = new Query();
        List<PresetPrice> presetPrices = mongoTemplate.findAllAndRemove(query,PresetPrice.class,"contract_preset_price_"+symbol+"_"+type);
        if(presetPrices!=null && presetPrices.size()>0){
            return presetPrices.get(0);
        }else {
            return null;
        }
    }
}
