package com.wikex.wikex.option.engine;

import com.wikex.wikex.constant.ContractOptionOrderDirection;
import com.wikex.wikex.option.handler.MarketHandler;
import com.wikex.wikex.option.job.ExchangePushJob;
import com.wikex.wikex.option.service.ContractOptionCoinService;
import com.wikex.wikex.option.service.ContractOptionOrderService;
import com.wikex.wikex.pojo.*;
import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


public class ContractOptionCoinMatch {

    private Logger logger = LoggerFactory.getLogger(WebSocketClient.class);

    private String symbol;                                           
    private String baseSymbol;                                       
    private String coinSymbol;                                       
    private CoinThumb thumb;                                         
    private LinkedList<ContractOptionTrade> lastedTradeList;               
    private int lastedTradeListSize = 50;

    private long lastUpdateTime = 0L;                                
    private boolean isTriggerComplete = true;                        
    private BigDecimal nowPrice = BigDecimal.ZERO;                   

    private ContractOptionCoinService contractOptionCoinService;                  
    private ContractOptionOrderService contractOptionOrderService;                

    private List<MarketHandler> handlers;                             
    private ExchangePushJob exchangePushJob;                          

    
    private OptionTradePlate sellTradePlate;
    
    private OptionTradePlate buyTradePlate;

    private boolean isStarted = false;                                

    
    public ContractOptionCoinMatch(String symbol) {
        this.symbol = symbol;
        this.coinSymbol = symbol.split("/")[0];
        this.baseSymbol = symbol.split("/")[1];
        this.handlers = new ArrayList<>();
        this.lastedTradeList = new LinkedList<>();
        this.buyTradePlate = new OptionTradePlate(symbol, ContractOptionOrderDirection.BUY);
        this.sellTradePlate = new OptionTradePlate(symbol, ContractOptionOrderDirection.SELL);
        
        this.initializeThumb();
    }

    
    public void run(){
        this.isStarted = true;
    }

    
    public void refreshPlate(List<TradePlateItem> buyPlateItems, List<TradePlateItem> sellPlateItems) {
        if(!this.isStarted) return;

        this.buyTradePlate.setItems(buyPlateItems);
        this.sellTradePlate.setItems(sellPlateItems);

        this.exchangePushJob.addPlates(symbol, sellTradePlate);
        this.exchangePushJob.addPlates(symbol, buyTradePlate);
    }

    
    public void refreshThumb(CoinThumb thumb) {
        if(!this.isStarted) return;

        this.thumb.setHigh(thumb.getHigh());
        this.thumb.setLow(thumb.getLow());
        this.thumb.setOpen(thumb.getClose());
        this.thumb.setClose(thumb.getClose());
        this.thumb.setTurnover(thumb.getTurnover());
        this.thumb.setVolume(thumb.getVolume());
        this.thumb.setUsdRate(thumb.getClose());
        
        this.thumb.setChange(thumb.getClose().subtract(thumb.getOpen()));
        if(thumb.getOpen().compareTo(BigDecimal.ZERO) > 0) {
            this.thumb.setChg(this.thumb.getChange().divide(this.thumb.getOpen(), 4, RoundingMode.UP));
        }

        
        
        handleCoinThumb();
    }

    
    public void refreshPrice(BigDecimal newPrice) {
        if(!this.isStarted) return;

        long currentTime = Calendar.getInstance().getTimeInMillis();
        
        if(currentTime - lastUpdateTime > 1000) {
            lastUpdateTime = currentTime;

            
            if(this.nowPrice.compareTo(newPrice) == 0) {
                return;
            }
            
            this.nowPrice = newPrice;
        }
    }

    
    public void refreshLastedTrade(List<ContractOptionTrade> tradeArrayList) {
        for(ContractOptionTrade trade : tradeArrayList) {
            if(lastedTradeList.size() > lastedTradeListSize) {
                this.lastedTradeList.removeLast();
                this.lastedTradeList.addFirst(trade);
            }else{
                this.lastedTradeList.addFirst(trade);
            }
        }
        
        
        this.exchangePushJob.addTrades(symbol, tradeArrayList);
    }

    
    public void initializeThumb() {
        this.thumb = new CoinThumb();
        this.thumb.setChg(BigDecimal.ZERO);                 
        this.thumb.setChange(BigDecimal.ZERO);              
        this.thumb.setOpen(BigDecimal.ZERO);                
        this.thumb.setClose(BigDecimal.ZERO);               
        this.thumb.setHigh(BigDecimal.ZERO);                
        this.thumb.setLow(BigDecimal.ZERO);                 
        this.thumb.setBaseUsdRate(BigDecimal.valueOf(6.98)); 
        this.thumb.setLastDayClose(BigDecimal.ZERO);        
        this.thumb.setSymbol(this.symbol);                  
        this.thumb.setUsdRate(BigDecimal.valueOf(6.98));     
        this.thumb.setZone(0);                              
        this.thumb.setVolume(BigDecimal.ZERO);              
        this.thumb.setTurnover(BigDecimal.ZERO);            
    }

    public void handleCoinThumb() {
        for (MarketHandler storage : handlers) {
            storage.handleTrade(symbol, thumb);
        }
    }

    public void handleKLineStorage(KLine kLine) {
        for (MarketHandler storage : handlers) {
            storage.handleKLine(symbol, kLine);
        }
    }
    
    public String getSymbol() { return this.symbol; }
    
    public String getCoinSymbol() { return this.coinSymbol; }
    
    public String getBaseSymbol() { return this.baseSymbol; }
    
    public BigDecimal getNowPrice() { return this.nowPrice; }
    
    public CoinThumb getThumb() { return this.thumb; }
    
    public List<ContractOptionTrade> getLastedTradeList() { return this.lastedTradeList; }
    
    public OptionTradePlate getTradePlate(ContractOptionOrderDirection direction){
        if(direction == ContractOptionOrderDirection.BUY){
            return buyTradePlate;
        }
        else{
            return sellTradePlate;
        }
    }
    
    public void addHandler(MarketHandler storage) {
        handlers.add(storage);
    }
    public void setExchangePushJob(ExchangePushJob job) { this.exchangePushJob = job; }
}
