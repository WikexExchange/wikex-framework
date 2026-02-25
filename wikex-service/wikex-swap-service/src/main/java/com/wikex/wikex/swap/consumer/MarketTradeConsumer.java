package com.wikex.wikex.swap.consumer;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.pojo.*;
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
@RocketMQMessageListener(topic = "market-trade", consumerGroup = "market-trade-swap")
public class MarketTradeConsumer implements RocketMQListener<String> {

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
        Map pMap = JSON.parseObject(content, Map.class);
        if(pMap==null){
            return;
        }
        String tSymbol =  pMap.get("symbol")!=null?pMap.get("symbol").toString():"";
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
                    if (symbol.equalsIgnoreCase(tSymbol)) {
                        Object trades1 = pMap.get("trades");
                        if(trades1==null){
                            continue;
                        }
                        List<ExchangeTrade> trades = JSON.parseArray(JSON.toJSONString(trades1),ExchangeTrade.class);
                        List<ContractTrade> tradeArrayList = new ArrayList<ContractTrade>();
                        if(trades!=null && trades.size()>0){
                            List<Poke> pokes = marketService.findPokeAndRemove(symbol,"trade",null);
                            for (ExchangeTrade trade1 : trades) {
                                BigDecimal amount = trade1.getAmount();
                                BigDecimal price =  trade1.getPrice();
                                int direction = trade1.getDirection().getCode();
                                long time = trade1.getTime();
                                
                                ContractTrade trade = new ContractTrade();
                                trade.setAmount(amount);
                                trade.setPrice(price);
                                if(direction == 0) {
                                    trade.setDirection(ContractOrderDirection.BUY);
                                    trade.setBuyOrderId(trade1.getBuyOrderId());
                                    trade.setBuyTurnover(amount.multiply(price));
                                }else{
                                    trade.setDirection(ContractOrderDirection.SELL);
                                    trade.setSellOrderId(trade1.getSellOrderId());
                                    trade.setSellTurnover(amount.multiply(price));
                                }
                                trade.setSymbol(symbol);
                                trade.setTime(time);
                                tradeArrayList.add(trade);
                            }





























                            
                            this.matchFactory.getContractCoinMatch(symbol).refreshLastedTrade(tradeArrayList);
                        }
                    }
                }
            }
        }
    }
}
