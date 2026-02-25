package com.wikex.wikex.coinswap.consumer;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.coinswap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.coinswap.service.ContractMarketService;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.Poke;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RocketMQMessageListener(topic = "market-thumb", consumerGroup = "market-thumb-coinswap")
public class CoinMarketThumbConsumer implements RocketMQListener<String> {

    private ArrayList<String> subCoinList = new ArrayList<String>();

    @Autowired
    private ContractCoinMatchFactory matchFactory;
    @Autowired
    private ContractMarketService marketService;
    @Value("${platformCoins}")
    private String platformCoins;

    @Override
    public void onMessage(String content) {
        
        if(content==null){
            return;
        }
        CoinThumb thumb = JSON.parseObject(content, CoinThumb.class);
        if(thumb==null){
            return;
        }
        if (null != this.matchFactory.getMatchMap() && this.matchFactory.getMatchMap().size() > 0) {
            
            for(String symbol : this.matchFactory.getMatchMap().keySet()) {
                
                if(platformCoins.contains(symbol.split("/")[0]) || platformCoins.contains(symbol.split("/")[1])){
                    if(!subCoinList.contains(symbol)){
                        subCoinList.add(symbol);
                    }
                }
            }
            if(subCoinList.size()>0){
                
                String symbol = thumb.getSymbol();
                if(subCoinList.contains(symbol)){
                    List<Poke> pokes = marketService.findPokeAndRemove(symbol,"detail",null);
                    BigDecimal lowPokePrice = null;
                    BigDecimal highPokePrice = null;
                    if(pokes!=null && pokes.size()>0){
                        for (Poke poke : pokes) {
                            BigDecimal price = BigDecimal.valueOf(Double.parseDouble(poke.getPrice()));
                            if(lowPokePrice == null){
                                lowPokePrice = price;
                            }else if(lowPokePrice!=null){
                                if(lowPokePrice.compareTo(price)==1){
                                    lowPokePrice = price;
                                }
                            }

                            if(highPokePrice == null){
                                highPokePrice = price;
                            }else if(highPokePrice!=null){
                                if(highPokePrice.compareTo(price)==-1){
                                    highPokePrice = price;
                                }
                            }
                        }
                    }

                    BigDecimal high = highPokePrice==null ? thumb.getHigh() : highPokePrice.compareTo(thumb.getHigh())==1?highPokePrice:thumb.getHigh();
                    BigDecimal low = lowPokePrice==null ? thumb.getLow() : lowPokePrice.compareTo(thumb.getLow())==-1?lowPokePrice:thumb.getLow();
                    thumb.setHigh(high);
                    thumb.setLow(low);
                    this.matchFactory.getContractCoinMatch(symbol).refreshThumb(thumb);
                    if(lowPokePrice!=null && highPokePrice!=null && lowPokePrice.compareTo(highPokePrice)==0){
                        highPokePrice = null;
                    }
                    
                    
                    if(lowPokePrice == null && highPokePrice == null){
                        this.matchFactory.getContractCoinMatch(symbol).refreshPrice(thumb.getClose());
                    }else {
                        if(lowPokePrice!=null){
                            this.matchFactory.getContractCoinMatch(symbol).refreshPrice(lowPokePrice);
                            
                        }
                        if(highPokePrice!=null){
                            this.matchFactory.getContractCoinMatch(symbol).refreshPrice(highPokePrice);
                            
                        }
                    }
                }
            }
        }
    }
}
