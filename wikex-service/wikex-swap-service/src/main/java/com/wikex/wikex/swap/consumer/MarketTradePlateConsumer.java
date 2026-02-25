package com.wikex.wikex.swap.consumer;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOrderPattern;
import com.wikex.wikex.pojo.TradePlate;
import com.wikex.wikex.pojo.TradePlateItem;
import com.wikex.wikex.swap.engine.ContractCoinMatchFactory;
import com.wikex.wikex.swap.entity.ContractCoin;
import com.wikex.wikex.swap.entity.ContractOrderEntrust;
import com.wikex.wikex.swap.entity.MemberContractWallet;
import com.wikex.wikex.swap.service.ContractCoinService;
import com.wikex.wikex.swap.service.MemberContractWalletService;
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
@RocketMQMessageListener(topic = "market-trade-plate", consumerGroup = "market-trade-plate-swap")
public class MarketTradePlateConsumer implements RocketMQListener<String> {

    private ArrayList<String> subCoinList = new ArrayList<String>();

    @Autowired
    private ContractCoinMatchFactory matchFactory;
    @Value("${platformCoins}")
    private String platformCoins;

    @Override
    public void onMessage(String content) {

        if(content==null){
            return;
        }
        Map pMap = JSON.parseObject(content, Map.class);
        if(pMap==null){
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
                
                for (String symbol : subCoinList) {
                    String pSymbol = pMap.get("symbol")!=null?pMap.get("symbol").toString():"";
                    if(symbol!=null && symbol.equalsIgnoreCase(pSymbol)){
                        Object plates = pMap.get("plates");
                        if(plates!=null){
                            TradePlate tradePlate =JSON.parseObject(JSON.toJSONString(plates),TradePlate.class);
                            List<TradePlateItem> buyItems = new ArrayList<>();
                            List<TradePlateItem> sellItems = new ArrayList<>();
                            if(tradePlate.getDirection().getCode()==0){
                                buyItems = tradePlate.getItems();
                            }else{
                                sellItems = tradePlate.getItems();
                            }
                            if(sellItems.size()>0 || buyItems.size()>0){
                                
                                this.matchFactory.getContractCoinMatch(symbol).refreshPlate(buyItems, sellItems);
                            }
                        }

                    }

                }
            }
        }
    }
}
