package com.wikex.wikex.option.job;

import com.wikex.wikex.constant.ContractOptionOrderDirection;
import com.wikex.wikex.option.handler.NettyHandler;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ContractOptionTrade;
import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.OptionTradePlate;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ExchangePushJob {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private NettyHandler nettyHandler;

    private Map<String,List<ContractOptionTrade>> tradesQueue = new HashMap<>();
    private Map<String,List<OptionTradePlate>> plateQueue = new HashMap<>();
    private Map<String,List<CoinThumb>> thumbQueue = new HashMap<>();

    
    
    
    
    

    public void addTrades(String symbol, List<ContractOptionTrade> trades){
        List<ContractOptionTrade> list = tradesQueue.get(symbol);
        if(list == null){
            list = new ArrayList<>();
            tradesQueue.put(symbol,list);
        }
        synchronized (list) {
            list.addAll(trades);
        }
    }

    public void addThumb(String symbol, CoinThumb thumb){
        List<CoinThumb> list = thumbQueue.get(symbol);
        if(list == null){
            list = new ArrayList<>();
            thumbQueue.put(symbol,list);
        }
        synchronized (list) {
            list.add(thumb);
        }
    }

    public void addPlates(String symbol, OptionTradePlate plate){
        List<OptionTradePlate> list = plateQueue.get(symbol);
        if(list == null){
            list = new ArrayList<>();
            plateQueue.put(symbol,list);
        }
        synchronized (list) {
            list.add(plate);
        }
    }

    
    public void pushTickKline(String symbol, KLine line){
        messagingTemplate.convertAndSend("/topic/option/kline/"+symbol,line);
    }


    @XxlJob("pushTrade")
    public void pushTrade(){
        Iterator<Map.Entry<String,List<ContractOptionTrade>>> entryIterator = tradesQueue.entrySet().iterator();
        while (entryIterator.hasNext()){
            Map.Entry<String,List<ContractOptionTrade>> entry =  entryIterator.next();
            String symbol = entry.getKey();
            List<ContractOptionTrade> trades = entry.getValue();
            if(trades.size() > 0){
                synchronized (trades) {
                    messagingTemplate.convertAndSend("/topic/option/trade/" + symbol, trades);
                    trades.clear();
                }
            }
        }
    }



    @XxlJob("pushPlate")
    public void pushPlate(){
        Iterator<Map.Entry<String,List<OptionTradePlate>>> entryIterator = plateQueue.entrySet().iterator();
        while (entryIterator.hasNext()){
            Map.Entry<String,List<OptionTradePlate>> entry =  entryIterator.next();
            String symbol = entry.getKey();
            List<OptionTradePlate> plates = entry.getValue();
            if(plates.size() > 0){
                boolean hasPushAskPlate = false;
                boolean hasPushBidPlate = false;
                synchronized (plates) {
                    for(OptionTradePlate plate:plates) {
                        if(plate.getDirection() == ContractOptionOrderDirection.BUY && !hasPushBidPlate) {
                            hasPushBidPlate = true;
                        }
                        else if(plate.getDirection() == ContractOptionOrderDirection.SELL && !hasPushAskPlate){
                            hasPushAskPlate = true;
                        }
                        else {
                            continue;
                        }
                        
                        messagingTemplate.convertAndSend("/topic/option/trade-plate/" + symbol, plate.toJSON(24));
                        
                        messagingTemplate.convertAndSend("/topic/option/trade-depth/" + symbol, plate.toJSON(50));
                        
                        nettyHandler.handlePlate(symbol, plate);
                    }
                    plates.clear();
                }
            }
        }
    }


    @XxlJob("pushThumb")
    public void pushThumb(){
        Iterator<Map.Entry<String,List<CoinThumb>>> entryIterator = thumbQueue.entrySet().iterator();
        while (entryIterator.hasNext()){
            Map.Entry<String,List<CoinThumb>> entry =  entryIterator.next();
            String symbol = entry.getKey();
            List<CoinThumb> thumbs = entry.getValue();
            if(thumbs.size() > 0){
                synchronized (thumbs) {
                    messagingTemplate.convertAndSend("/topic/option/thumb",thumbs.get(thumbs.size() - 1));
                    thumbs.clear();
                }
            }
        }
    }
}
