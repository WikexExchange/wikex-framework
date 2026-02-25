package com.wikex.wikex.coinswap.job;

import com.wikex.wikex.coinswap.handler.NettyHandler;
import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.pojo.CoinThumb;
import com.wikex.wikex.pojo.ContractTrade;
import com.wikex.wikex.pojo.KLine;
import com.wikex.wikex.pojo.TradePlate;
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

    private Map<String,List<ContractTrade>> tradesQueue = new HashMap<>();
    private Map<String,List<TradePlate>> plateQueue = new HashMap<>();
    private Map<String,List<CoinThumb>> thumbQueue = new HashMap<>();

    
    
    
    
    

    public void addTrades(String symbol, List<ContractTrade> trades){
        List<ContractTrade> list = tradesQueue.get(symbol);
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

    public void addPlates(String symbol, TradePlate plate){
        List<TradePlate> list = plateQueue.get(symbol);
        if(list == null){
            list = new ArrayList<>();
            plateQueue.put(symbol,list);
        }
        synchronized (list) {
            list.add(plate);
        }
    }

    
    public void pushTickKline(String symbol, KLine kline){
        messagingTemplate.convertAndSend("/topic/swap/kline/"+symbol, kline);
        nettyHandler.handleKLine(symbol, kline);
    }

    @XxlJob("pushTrade")
    public void pushTrade(){
        Iterator<Map.Entry<String,List<ContractTrade>>> entryIterator = tradesQueue.entrySet().iterator();
        while (entryIterator.hasNext()){
            Map.Entry<String,List<ContractTrade>> entry =  entryIterator.next();
            String symbol = entry.getKey();
            List<ContractTrade> trades = entry.getValue();
            if(trades.size() > 0){
                synchronized (trades) {
                    messagingTemplate.convertAndSend("/topic/swap/trade/" + symbol, trades);
                    nettyHandler.handleTrades(symbol, trades, null);
                    trades.clear();
                }
            }
        }
    }



    @XxlJob("pushPlate")
    public void pushPlate(){
        Iterator<Map.Entry<String,List<TradePlate>>> entryIterator = plateQueue.entrySet().iterator();
        while (entryIterator.hasNext()){
            Map.Entry<String,List<TradePlate>> entry =  entryIterator.next();
            String symbol = entry.getKey();
            List<TradePlate> plates = entry.getValue();
            if(plates.size() > 0){
                boolean hasPushAskPlate = false;
                boolean hasPushBidPlate = false;
                synchronized (plates) {
                    for(TradePlate plate:plates) {
                        if(plate.getDirection() == ContractOrderDirection.BUY && !hasPushBidPlate) {
                            hasPushBidPlate = true;
                        }
                        else if(plate.getDirection() == ContractOrderDirection.SELL && !hasPushAskPlate){
                            hasPushAskPlate = true;
                        }
                        else {
                            continue;
                        }
                        if(plate.getItems().size()>0) {
                            
                            messagingTemplate.convertAndSend("/topic/swap/trade-plate/" + symbol, plate.toJSON(24));
                            
                            messagingTemplate.convertAndSend("/topic/swap/trade-depth/" + symbol, plate.toJSON(50));
                            
                            nettyHandler.handlePlate(symbol, plate);
                        }
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
                    messagingTemplate.convertAndSend("/topic/swap/thumb",thumbs.get(thumbs.size() - 1));
                    thumbs.clear();
                }
            }
        }
    }
}
