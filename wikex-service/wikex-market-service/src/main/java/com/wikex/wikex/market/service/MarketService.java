package com.wikex.wikex.market.service;

import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.pojo.KLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class MarketService {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public List<KLine> findAllKLine(String symbol, String peroid) {
        Sort sort = Sort.by(Sort.Direction.DESC, "time");
        Query query = new Query().with(sort).limit(1000);

        return mongoTemplate.find(query, KLine.class, "exchange_kline_" + symbol + "_" + peroid);
    }

    public List<KLine> findAllKLine(String symbol, long fromTime, long toTime, String period) {
        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = Sort.by(Sort.Direction.ASC, "time");
        Query query = new Query(criteria).with(sort);
        List<KLine> kLines = mongoTemplate.find(query, KLine.class, "exchange_kline_" + symbol + "_" + period);
        return kLines;
    }

    public ExchangeTrade findFirstTrade(String symbol, long fromTime, long toTime) {
        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = Sort.by(Sort.Direction.ASC, "time");
        Query query = new Query(criteria).with(sort);
        return mongoTemplate.findOne(query, ExchangeTrade.class, "exchange_trade_" + symbol);
    }

    public ExchangeTrade findLastTrade(String symbol, long fromTime, long toTime) {
        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = Sort.by(Sort.Direction.DESC, "time");
        Query query = new Query(criteria).with(sort);
        return mongoTemplate.findOne(query, ExchangeTrade.class, "exchange_trade_" + symbol);
    }

    public ExchangeTrade findTrade(String symbol, long fromTime, long toTime, Sort.Order order) {
        Criteria criteria = Criteria.where("time").gte(fromTime).andOperator(Criteria.where("time").lte(toTime));
        Sort sort = Sort.by(order);
        Query query = new Query(criteria).with(sort);
        return mongoTemplate.findOne(query, ExchangeTrade.class, "exchange_trade_" + symbol);
    }

    public List<ExchangeTrade> findTradeByTimeRange(String symbol, long timeStart, long timeEnd) {
        Criteria criteria = Criteria.where("time").gte(timeStart).andOperator(Criteria.where("time").lt(timeEnd));
        Sort sort = Sort.by(Sort.Direction.ASC, "time");
        Query query = new Query(criteria).with(sort);

        return mongoTemplate.find(query, ExchangeTrade.class, "exchange_trade_" + symbol);
    }

    public void saveKLine(String symbol, KLine kLine) {
        mongoTemplate.insert(kLine, "exchange_kline_" + symbol + "_" + kLine.getPeriod());
    }

    /**
     * get trade volume between timeStart and timeEnd
     *
     * @param symbol
     * @param timeStart
     * @param timeEnd
     * @return
     */
    public BigDecimal findTradeVolume(String symbol, long timeStart, long timeEnd) {
        // Tạo cache key dựa trên symbol, timeStart và timeEnd
        String cacheKey = "trade_volume:" + symbol + ":" + timeStart + ":" + timeEnd;

        // Kiểm tra cache trước
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String cachedValue = valueOperations.get(cacheKey);
        if (cachedValue != null) {
            return new BigDecimal(cachedValue);
        }

        Criteria criteria = Criteria.where("time").gt(timeStart)
                .andOperator(Criteria.where("time").lte(timeEnd));
        //.andOperator(Criteria.where("volume").gt(0));
        Sort sort = Sort.by(Sort.Direction.ASC,"time");
        Query query = new Query(criteria).with(sort);
        List<KLine> kLines =  mongoTemplate.find(query,KLine.class,"exchange_kline_"+symbol+"_1min");
        BigDecimal totalVolume = BigDecimal.ZERO;
        for(KLine kLine:kLines){
            totalVolume = totalVolume.add(kLine.getVolume());
        }

        // Lưu vào Redis với TTL 1 phút
        valueOperations.set(cacheKey, totalVolume.toString(), 1, TimeUnit.MINUTES);

        return totalVolume;
    }
}
