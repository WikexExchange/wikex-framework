package com.wikex.wikex.second.engine;

import com.wikex.wikex.constant.ContractOrderDirection;
import com.wikex.wikex.constant.ContractSecondOrderStatus;
import com.wikex.wikex.pojo.*;
import com.wikex.wikex.second.entity.ContractSecondCoin;
import com.wikex.wikex.second.entity.ContractSecondOrder;
import com.wikex.wikex.second.handler.MarketHandler;
import com.wikex.wikex.second.job.ExchangePushJob;
import com.wikex.wikex.second.service.ContractSecondCoinService;
import com.wikex.wikex.second.service.ContractSecondOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;


public class ContractCoinMatch {

    private Logger logger = LoggerFactory.getLogger(ContractCoinMatch.class);

    private String symbol;                                           
    private String baseSymbol;                                       
    private String coinSymbol;                                       
    private CoinThumb thumb;                                         
    private LinkedList<ContractTrade> lastedTradeList;               
    private int lastedTradeListSize = 50;

    private long lastUpdateTime = 0L;                                
    private boolean isTriggerComplete = true;                        
    private BigDecimal nowPrice = BigDecimal.ZERO;                   

    private ContractSecondCoinService contractSecondCoinService;                  
    private ContractSecondOrderService contractSecondOrderService;  
    private ContractSecondCoin contractSecondCoin;

    private List<ContractSecondOrder> contractSecondOrders = new ArrayList<>();      

    private List<MarketHandler> handlers;                             
    private ExchangePushJob exchangePushJob;                          

    
    private TradePlate sellTradePlate;
    
    private TradePlate buyTradePlate;

    private boolean isStarted = false;                                


    
    public ContractCoinMatch(String symbol) {
        this.symbol = symbol;
        this.coinSymbol = symbol.split("/")[0];
        this.baseSymbol = symbol.split("/")[1];
        this.handlers = new ArrayList<>();
        this.lastedTradeList = new LinkedList<>();
        this.buyTradePlate = new TradePlate(symbol, ContractOrderDirection.BUY);
        this.sellTradePlate = new TradePlate(symbol, ContractOrderDirection.SELL);
        
        this.initializeThumb();
    }


    
 public void run(){
    logger.info(symbol + " Contract engine starting, loading database orders...");
    contractSecondCoin = contractSecondCoinService.findBySymbol(symbol);
    if(contractSecondCoin == null) {
        logger.info(contractSecondCoin.getSymbol() + " Engine failed to start, contract trading pair not found");
        return;
    }
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
        logger.info("Trade: {}, entered; isTriggerComplete:{}",symbol,isTriggerComplete);
        
        if(!this.isStarted) return;

        



        long currentTime = Calendar.getInstance().getTimeInMillis();
        
        lastUpdateTime = currentTime;

        synchronized (this.nowPrice) {
            



            this.nowPrice = newPrice;
        }
        
        isTriggerComplete = false;
        this.process(newPrice);
    }

    
    public void refreshLastedTrade(List<ContractTrade> tradeArrayList) {
        synchronized (lastedTradeList) {
            for (ContractTrade trade : tradeArrayList) {
                if (lastedTradeList.size() > lastedTradeListSize) {
                    this.lastedTradeList.removeLast();
                    this.lastedTradeList.addFirst(trade);
                } else {
                    this.lastedTradeList.addFirst(trade);
                }
            }

            
            this.exchangePushJob.addTrades(symbol, tradeArrayList);
        }
    }

    
    public void process(BigDecimal newPrice) {
        long startTick = System.currentTimeMillis();
        this.processOrders(newPrice);              
        this.isTriggerComplete = true;
       logger.info("Trading pair {}, close position processing time: {}", symbol, System.currentTimeMillis() - startTick);

    }


    
    public void processOrders(BigDecimal newPrice) {
        
        synchronized (contractSecondOrders){
            contractSecondOrders = contractSecondOrderService.findBySymbolAndStatusAndCloseTime(symbol, ContractSecondOrderStatus.OPEN,new Date());
            if(contractSecondOrders != null && contractSecondOrders.size()>0) {
                for (ContractSecondOrder order : contractSecondOrders) {
                    contractSecondOrderService.closeOrder(order,newPrice);
                }
            }
        }
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
    
    public List<ContractTrade> getLastedTradeList() { return this.lastedTradeList; }
    
    public TradePlate getTradePlate(ContractOrderDirection direction){
        if(direction == ContractOrderDirection.BUY){
            return buyTradePlate;
        }
        else{
            return sellTradePlate;
        }
    }
    
    public void setContractSecondCoinService(ContractSecondCoinService contractSecondCoinService) { this.contractSecondCoinService = contractSecondCoinService; }
    
    
    public void addHandler(MarketHandler storage) {
        handlers.add(storage);
    }
    public void setExchangePushJob(ExchangePushJob job) { this.exchangePushJob = job; }

    public void setContractSecondOrderService(ContractSecondOrderService contractSecondOrderService){
        this.contractSecondOrderService = contractSecondOrderService;
    }

    
    public void updateContractCoin(ContractSecondCoin coin) {
        synchronized (contractSecondCoin) {
            contractSecondCoin = coin;
        }
    }

}
