package com.wikex.wikex.swap.consumer;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.Poke;
import com.wikex.wikex.pojo.TradePlate;
import com.wikex.wikex.pojo.TradePlateItem;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.service.ContractMarketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RocketMQMessageListener(topic = "market-thumb", consumerGroup = "market-thumb-swap")
public class MarketThumbConsumer implements RocketMQListener<String> {

    private ArrayList<String> subCoinList = new ArrayList<String>();

    @Autowired
    private ContractCoinMatchFactory matchFactory;
    @Autowired
    private ContractMarketService marketService;
    @Value("${platformCoins}")
    private String platformCoins;

    @Override
    public void onMessage(String content) {
//        
        if(content==null){
            return;
        }
        CoinThumb thumb = JSON.parseObject(content, CoinThumb.class);
        if(thumb==null){
            return;
        }
        if (null != this.matchFactory.getMatchMap() && this.matchFactory.getMatchMap().size() > 0) {
            // Initialize subCoinList
            for(String symbol : this.matchFactory.getMatchMap().keySet()) {
                // Platform coin
                if(platformCoins.contains(symbol.split("/")[0]) || platformCoins.contains(symbol.split("/")[1])){
                    if(!subCoinList.contains(symbol)){
                        subCoinList.add(symbol);
                    }
                }
            }
            if(subCoinList.size()>0){
                // Subscribe market overview
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
                                    lowPokePrice = price;// Take the lowest price
                                }
                            }

                            if(highPokePrice == null){
                                highPokePrice = price;
                            }else if(highPokePrice!=null){
                                if(highPokePrice.compareTo(price)==-1){
                                    highPokePrice = price;// Take the highest price
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
                        highPokePrice = null;// If lowPokePrice == highPokePrice then remove one
                    }
                    
                    // Entrust trigger or liquidation
                    if(lowPokePrice == null && highPokePrice == null){
                        this.matchFactory.refreshPrice(symbol,thumb.getClose());
                    }else {
                        if(lowPokePrice!=null){
                            this.matchFactory.refreshPrice(symbol,lowPokePrice);
                            
                        }
                        if(highPokePrice!=null){
                            this.matchFactory.refreshPrice(symbol,highPokePrice);
                            
                        }
                    }
                }
            }
        }
    }
}
