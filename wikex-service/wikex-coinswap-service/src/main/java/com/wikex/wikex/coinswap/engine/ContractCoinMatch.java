package com.wikex.wikex.coinswap.engine;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.config.SnowflakeConfig;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.pojo.*;
import com.wikex.wikex.coinswap.entity.ContractCoinCoin;
import com.wikex.wikex.coinswap.entity.ContractOrderEntrustCoin;
import com.wikex.wikex.coinswap.entity.MemberContractWalletCoin;
import com.wikex.wikex.coinswap.handler.MarketHandler;
import com.wikex.wikex.coinswap.job.ExchangePushJob;
import com.wikex.wikex.coinswap.service.ContractCoinCoinService;
import com.wikex.wikex.coinswap.service.ContractOrderEntrustCoinService;
import com.wikex.wikex.coinswap.service.MemberContractWalletCoinService;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.util.DateUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;

/**
 * Contract matching engine
 */
@Slf4j
public class ContractCoinMatch {

    private String symbol;                                           // Trading pair: BTC/USDT
    private String baseSymbol;                                       // Base currency: USDT
    private String coinSymbol;                                       // Coin: BTC
    private CoinThumb thumb;                                         // Trading pair market data
    private LinkedList<ContractTrade> lastedTradeList;               // Latest trade details
    private int lastedTradeListSize = 50;

    private long lastUpdateTime = 0L;                                // Last price update time (mainly used to control the price refresh cycle, because websocket price updates are too fast)
    private boolean isTriggerComplete = true;                        // Whether price refresh is complete, triggers orders and liquidation
    private BigDecimal nowPrice = BigDecimal.ZERO;                   // Current latest price

    private ContractCoinCoinService contractCoinService;                  // Contract coin service
    private ContractOrderEntrustCoinService contractOrderEntrustService;  // Contract entrust order service
    private MemberTransactionFeign memberTransactionFeign;
    private MemberContractWalletCoinService memberContractWalletService;
    private ContractCoinCoin contractCoin;

    private SnowflakeConfig snowflakeConfig;

    private List<ContractOrderEntrustCoin> contractOrderEntrustList = new ArrayList<>();      // Entrust list (planned orders)
    private List<MemberContractWalletCoin> memberContractWalletList = new ArrayList<>();      // User position information

    private List<MarketHandler> handlers;                             // Handlers for market, summary, etc.
    private ExchangePushJob exchangePushJob;                          // Push job

    // Sell order book information
    private TradePlate sellTradePlate;
    // Buy order book information
    private TradePlate buyTradePlate;

    private boolean isStarted = false;                                // Whether initialization has been completed (used to fetch unprocessed orders from DB at startup; if not completed, do not allow processing)

    private LinkedList<ContractOrderEntrustCoin> openOrderList = new LinkedList<ContractOrderEntrustCoin>(); // Open position orders
    private LinkedList<ContractOrderEntrustCoin> closeOrderList = new LinkedList<ContractOrderEntrustCoin>(); // Close position orders

    private LinkedList<ContractOrderEntrustCoin> openOrderSpotList = new LinkedList<ContractOrderEntrustCoin>(); // Open position stop profit/stop loss orders
    private LinkedList<ContractOrderEntrustCoin> closeOrderSpotList = new LinkedList<ContractOrderEntrustCoin>(); // Close position stop profit/stop loss orders

    /**
     * Constructor
     * @param symbol
     */
    public ContractCoinMatch(String symbol) {
        this.symbol = symbol;
        this.coinSymbol = symbol.split("/")[0];
        this.baseSymbol = symbol.split("/")[1];
        this.handlers = new ArrayList<>();
        this.lastedTradeList = new LinkedList<>();
        this.buyTradePlate = new TradePlate(symbol, ContractOrderDirection.BUY);
        this.sellTradePlate = new TradePlate(symbol, ContractOrderDirection.SELL);
        // Initialize market data
        this.initializeThumb();
    }

    public void trade(ContractOrderEntrustCoin order) throws ParseException {
        if(!this.isStarted) return;
        if(!symbol.equalsIgnoreCase(order.getSymbol())){
            
            return ;
        }
        if(order.getVolume().compareTo(BigDecimal.ZERO) <=0 || order.getVolume().subtract(order.getTradedVolume()).compareTo(BigDecimal.ZERO)<=0){
            return ;
        }
        if(order.getEntrustType() == ContractOrderEntrustType.OPEN) { // Open position
            if(order.getType() == ContractOrderType.MARKET_PRICE) { // Market price, direct trade
                this.dealOpenOrder(order);
            }else if(order.getType() == ContractOrderType.LIMIT_PRICE) { // Limit price
                if(order.getDirection() == ContractOrderDirection.BUY && order.getEntrustPrice().compareTo(nowPrice) >= 0
                        || order.getDirection() == ContractOrderDirection.SELL && order.getEntrustPrice().compareTo(nowPrice) <= 0){ // Price better than current, direct trade
                    this.dealOpenOrder(order);
                }else{
                    // Put into monitoring list
                    synchronized (openOrderList) {
                        
                        openOrderList.addLast(order);
                    }
                }
            }else if(order.getType() == ContractOrderType.SPOT_LIMIT) { // Planned entrust
                synchronized (openOrderSpotList) {
                    
                    openOrderSpotList.add(order);
                }
            }
        }else if(order.getEntrustType() == ContractOrderEntrustType.CLOSE) { // Close position
            if(order.getType() == ContractOrderType.MARKET_PRICE) { // Market price, direct trade
                this.dealCloseOrder(order);
            }else if(order.getType() == ContractOrderType.LIMIT_PRICE) { // Limit price
                if(order.getDirection() == ContractOrderDirection.BUY && order.getEntrustPrice().compareTo(nowPrice) > 0
                        || order.getDirection() == ContractOrderDirection.SELL && order.getEntrustPrice().compareTo(nowPrice) < 0){ // Price better than current, direct trade
                    this.dealCloseOrder(order);
                }else{
                    // Put into monitoring list
                    synchronized (closeOrderList) {
                        
                        closeOrderList.addLast(order);
                    }
                }
            }else if(order.getType() == ContractOrderType.SPOT_LIMIT) { // Planned entrust
                synchronized (closeOrderSpotList) {
                    
                    closeOrderSpotList.add(order);
                }
            }
        }
    }

    /**
     * Process open position order
     * @param order
     */
    public void dealOpenOrder(ContractOrderEntrustCoin order){
        
        MemberContractWalletCoin memberContractWallet = memberContractWalletService.findByMemberIdAndContractCoin(order.getMemberId(), contractCoin);
        // Deduct fee (from frozen balance)
        memberContractWalletService.decreaseCoinFrozen(memberContractWallet.getId(), order.getOpenFee());
        // Contract trading pair fee income increases
        contractCoinService.increaseTotalOpenFee(contractCoin.getId(), order.getOpenFee());
        // Unified fee handling
        handleFee(order.getMemberId(), order.getOpenFee());
        // Add asset change record
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(BigDecimal.ZERO.subtract(order.getOpenFee()));
        memberTransaction.setMemberId(order.getMemberId());
        memberTransaction.setSymbol(contractCoin.getSymbol().split("/")[0]);
        memberTransaction.setType(TransactionType.CONTRACT_FEE.getCode());
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransactionFeign.save(memberTransaction);

        // Long: deduct margin to long margin account (reduce frozen balance)
        if (order.getDirection() == ContractOrderDirection.BUY) {
            memberContractWalletService.increaseCoinBuyPrincipalAmountWithFrozen(memberContractWallet.getId(), order.getPrincipalAmount());
        }
        // Short: deduct margin to short margin account (reduce available balance)
        if (order.getDirection() == ContractOrderDirection.SELL) {
            memberContractWalletService.increaseCoinSellPrincipalAmountWithFrozen(memberContractWallet.getId(), order.getPrincipalAmount());
        }
        // Calculate open price (slippage > market price used)
        BigDecimal openPrice = BigDecimal.ZERO;
        openPrice = nowPrice;
        if (order.getDirection() == ContractOrderDirection.BUY) { // Buy, slippage calculation, long, higher price trade
            if (contractCoin.getSpreadType() == 1) { // Spread type: percentage
                openPrice = nowPrice.add(nowPrice.multiply(contractCoin.getSpread())); // Trade at current/slippage price
            } else { // Spread type: fixed
                openPrice = nowPrice.add(contractCoin.getSpread());
            }
        } else { // Sell, slippage calculation, short, lower price trade
            if (contractCoin.getSpreadType() == 1) { // Spread type: percentage
                openPrice = nowPrice.subtract(nowPrice.multiply(contractCoin.getSpread())); // Trade at current/slippage price
            } else { // Spread type: fixed
                openPrice = nowPrice.subtract(contractCoin.getSpread());
            }
        }

        // Calculate average open price and update position average price
        // (current position size * position avg price + new volume * trade price) / (current position size + new volume)
        BigDecimal avaPrice = BigDecimal.ZERO;
        if (order.getDirection() == ContractOrderDirection.BUY) {
            // Previous position
            BigDecimal obp = memberContractWallet.getCoinBuyPosition().add(memberContractWallet.getCoinFrozenBuyPosition());
            BigDecimal nbp =order.getVolume();// New position
            BigDecimal tbp =obp.add(nbp);// Total position
            if(obp.compareTo(BigDecimal.ZERO)==0){
                avaPrice = openPrice;
            }else {
                avaPrice = tbp.multiply(openPrice.multiply(memberContractWallet.getCoinBuyPrice())).divide(
                        obp.multiply(openPrice).add((nbp.multiply(memberContractWallet.getCoinBuyPrice()))),
                        8, BigDecimal.ROUND_DOWN
                );
            }
            // Update long average price and position
            memberContractWalletService.updateCoinBuyPriceAndPosition(memberContractWallet.getId(), avaPrice, order.getVolume());
        } else {
            // Previous position
            BigDecimal osp = memberContractWallet.getCoinSellPosition().add(memberContractWallet.getCoinFrozenSellPosition());
            BigDecimal nsp =order.getVolume();// New position
            BigDecimal tsp =osp.add(nsp);// Total position
            if(osp.compareTo(BigDecimal.ZERO)==0){
                avaPrice = openPrice;
            }else {
                avaPrice = tsp.multiply(openPrice.multiply(memberContractWallet.getCoinSellPrice())).divide(
                        osp.multiply(openPrice).add((nsp.multiply(memberContractWallet.getCoinSellPrice()))),
                        8, BigDecimal.ROUND_DOWN
                );
            }

            // Update short average price and position
            memberContractWalletService.updateCoinSellPriceAndPosition(memberContractWallet.getId(), avaPrice, order.getVolume());
        }

        if(memberContractWallet.getCoinShareNumber().compareTo(order.getShareNumber()) != 0) {
            memberContractWalletService.updateShareNumber(memberContractWallet.getId(), order.getShareNumber());
        }

        order.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS); // Entrust status: traded
        order.setTradedVolume(order.getVolume()); // Set traded volume
        order.setTradedPrice(openPrice);
        contractOrderEntrustService.saveOrUpdate(order);

        // Sync latest user position data
        memberWalletChange(memberContractWallet.getId());
        //syncMemberPosition();
    }

    private BigDecimal getSellPL(MemberContractWalletCoin memberContractWallet, BigDecimal dealPrice) {
        return memberContractWallet.getCoinShareNumber().multiply(
                memberContractWallet.getCoinSellPosition().add(memberContractWallet.getCoinFrozenSellPosition())
        ).divide(memberContractWallet.getCoinSellPrice(), 8, BigDecimal.ROUND_DOWN).multiply(memberContractWallet.getCoinSellPrice().subtract(dealPrice)).divide(dealPrice, 8, BigDecimal.ROUND_DOWN);
    }

    private BigDecimal getCloseOrderFee(ContractOrderEntrustCoin order, MemberContractWalletCoin memberContractWallet, BigDecimal dealPrice) {
        return order.getVolume().multiply(memberContractWallet.getCoinShareNumber()).multiply(contractCoin.getCloseFee()).divide(dealPrice,8, BigDecimal.ROUND_DOWN);
    }

    private BigDecimal getBuyPL(BigDecimal newPrice, MemberContractWalletCoin wallet) {
        return wallet.getCoinShareNumber().multiply(
                wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition())
        ).divide(wallet.getCoinBuyPrice(), 8, BigDecimal.ROUND_DOWN).multiply(newPrice.subtract(wallet.getCoinBuyPrice())).divide(newPrice, 8, BigDecimal.ROUND_DOWN);
    }
    /**
     * Process close position order
     * @param order
     */
    public void dealCloseOrder(ContractOrderEntrustCoin order) {
        
        MemberContractWalletCoin memberContractWallet = memberContractWalletService.findByMemberIdAndContractCoin(order.getMemberId(), contractCoin);
        // Calculate slippage trade price (used for market order)
        BigDecimal dealPrice = nowPrice;
        if (order.getDirection() == ContractOrderDirection.BUY) { // Buy to close short, slippage calculation, higher price
            if (contractCoin.getSpreadType() == 1) { // Spread type: percentage
                dealPrice = nowPrice.add(nowPrice.multiply(contractCoin.getSpread())); // Trade at current/slippage price
            } else { // Spread type: fixed
                dealPrice = nowPrice.add(contractCoin.getSpread());
            }
        } else { // Sell, slippage calculation, short, lower price
            if (contractCoin.getSpreadType() == 1) { // Spread type: percentage
                dealPrice = nowPrice.subtract(nowPrice.multiply(contractCoin.getSpread())); // Trade at current/slippage price
            } else { // Spread type: fixed
                dealPrice = nowPrice.subtract(contractCoin.getSpread());
            }
        }

        if (order.getDirection() == ContractOrderDirection.BUY) { // Buy to close short
            // Close short - Short profit/loss calculation: (1 - current price / open avg price) * (available + frozen positions) * contract size
            BigDecimal pL = getSellPL(memberContractWallet, dealPrice);

            // Calculate margin to be deducted (close volume / total position * total margin)
            BigDecimal principalAmount = order.getPrincipalAmount();
            // Calculate close fee
            BigDecimal closeFee = getCloseOrderFee(order, memberContractWallet, dealPrice);

            // Deduct user's short frozen position and corresponding margin
            memberContractWalletService.decreaseCoinFrozenSellPositionAndPrincipalAmount(memberContractWallet.getId(), order.getVolume(), principalAmount);
            // Increase user's balance
            memberContractWalletService.increaseCoinBalance(memberContractWallet.getId(), principalAmount.add(pL).subtract(closeFee));
            // Platform increases close fee
            contractCoinService.increaseTotalCloseFee(contractCoin.getId(), closeFee);
            // Unified fee handling
            handleFee(memberContractWallet.getMemberId(), closeFee);
            // Set entrust order status
            order.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS);
            order.setTradedVolume(order.getVolume());
            order.setTradedPrice(dealPrice);
            order.setProfitAndLoss(pL);
            order.setCloseFee(closeFee);
            contractOrderEntrustService.saveOrUpdate(order);

            // Update platform PnL / update user account PnL
            if (pL.compareTo(BigDecimal.ZERO) > 0){
                memberContractWalletService.increaseCoinProfit(memberContractWallet.getId(), pL);
                contractCoinService.increaseTotalLoss(contractCoin.getId(), pL); // User profit, platform loss
            }
            if (pL.compareTo(BigDecimal.ZERO) < 0){
                memberContractWalletService.increaseCoinLoss(memberContractWallet.getId(), BigDecimal.ZERO.subtract(pL));
                contractCoinService.increaseTotalProfit(contractCoin.getId(), BigDecimal.ZERO.subtract(pL)); // User loss, platform profit
            }

            // Unified user PnL handling
            handlePl(memberContractWallet.getMemberId(), pL);

        } else { // Sell to close long
            // Close long - Long profit/loss calculation: (current price / open avg price - 1) * (available + frozen positions) * contract size
            BigDecimal pL = getBuyPL(dealPrice, memberContractWallet);

            // Calculate margin to be deducted (close volume / total position * total margin)
            BigDecimal principalAmount = order.getPrincipalAmount();
            // Calculate close fee
            BigDecimal closeFee = getCloseOrderFee(order, memberContractWallet, dealPrice);

            
            // Deduct user's long frozen position and margin
            memberContractWalletService.decreaseCoinFrozenBuyPositionAndPrincipalAmount(memberContractWallet.getId(), order.getVolume(), principalAmount);
            // Increase user's balance
            memberContractWalletService.increaseCoinBalance(memberContractWallet.getId(), principalAmount.add(pL).subtract(closeFee));
            // Platform increases close fee
            contractCoinService.increaseTotalCloseFee(contractCoin.getId(), closeFee);
            // Unified fee handling
            handleFee(memberContractWallet.getMemberId(), closeFee);

            if(memberContractWallet.getCoinShareNumber().compareTo(order.getShareNumber()) != 0) {
                memberContractWalletService.updateShareNumber(memberContractWallet.getId(), order.getShareNumber());
            }
            // Set entrust order status
            order.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS);
            order.setTradedVolume(order.getVolume());
            order.setTradedPrice(dealPrice);
            order.setCloseFee(closeFee);
            order.setProfitAndLoss(pL);
            
            contractOrderEntrustService.saveOrUpdate(order);

            // Update platform PnL / update user account PnL
            if (pL.compareTo(BigDecimal.ZERO) > 0){
                memberContractWalletService.increaseCoinProfit(memberContractWallet.getId(), pL);
                contractCoinService.increaseTotalLoss(contractCoin.getId(), pL); // User profit, platform loss
            }
            if (pL.compareTo(BigDecimal.ZERO) < 0){
                memberContractWalletService.increaseCoinLoss(memberContractWallet.getId(), BigDecimal.ZERO.subtract(pL));
                contractCoinService.increaseTotalProfit(contractCoin.getId(), BigDecimal.ZERO.subtract(pL)); // User loss, platform profit
            }

            // Unified user PnL handling
            handlePl(memberContractWallet.getMemberId(), pL);
        }

        // Sync latest user position data
        memberWalletChange(memberContractWallet.getId());
        // Sync latest user position data
        // syncMemberPosition();
    }

    /**
     * Start engine, load unprocessed orders
     */
    public void run(){
        
        contractCoin = contractCoinService.findBySymbol(symbol);
        if(contractCoin == null) {
            
            return;
        }
        // Query orders from database and load into list
        contractOrderEntrustList = contractOrderEntrustService.loadUnMatchOrders(contractCoin.getId());
        if(contractOrderEntrustList != null && contractOrderEntrustList.size() > 0) {
            
            for (ContractOrderEntrustCoin item : contractOrderEntrustList) {
                if (item.getEntrustType() == ContractOrderEntrustType.OPEN) { // Open order
                    if (item.getType() == ContractOrderType.SPOT_LIMIT) { // Planned entrust order (take-profit/stop-loss order)
                        openOrderSpotList.add(item);
                    } else {
                        openOrderList.add(item);
                    }
                } else { // Close order
                    if (item.getType() == ContractOrderType.SPOT_LIMIT) { // Planned entrust order (take-profit/stop-loss order)
                        closeOrderSpotList.add(item);
                    } else {
                        closeOrderList.add(item);
                    }
                }
            }
        }
        // Load user position info
        this.syncMemberPosition();
        this.isStarted = true;
    }

    /**
     * Update order book (buy/sell, Huobi Websocket provides 20 items)
     * @param buyPlateItems
     * @param sellPlateItems
     */
    public void refreshPlate(List<TradePlateItem> buyPlateItems, List<TradePlateItem> sellPlateItems) {
        if(!this.isStarted) return;

        if(buyPlateItems.size()>0){
            this.buyTradePlate.setItems(buyPlateItems);
            this.exchangePushJob.addPlates(symbol, buyTradePlate);
        }
        if(sellPlateItems.size()>0){
            this.sellTradePlate.setItems(sellPlateItems);
            this.exchangePushJob.addPlates(symbol, sellTradePlate);
        }

//        
    }

    /**
     * Update market data
     * @param thumb
     */
    public void refreshThumb(CoinThumb thumb) {
        if(!this.isStarted) return;

        this.thumb.setHigh(thumb.getHigh());
        this.thumb.setLow(thumb.getLow());
        this.thumb.setOpen(thumb.getClose());
        this.thumb.setClose(thumb.getClose());
        this.thumb.setTurnover(thumb.getTurnover());
        this.thumb.setVolume(thumb.getVolume());
        this.thumb.setUsdRate(thumb.getClose());
        // Calculate change (change amount and percentage, which may be negative)
        this.thumb.setChange(thumb.getClose().subtract(thumb.getOpen()));
        if(thumb.getOpen().compareTo(BigDecimal.ZERO) > 0) {
            this.thumb.setChg(this.thumb.getChange().divide(this.thumb.getOpen(), 4, RoundingMode.UP));
        }

//        
        // Push market data
        handleCoinThumb();
    }

    /**
     * Update price
     * When updating price, involves planned orders, take-profit/stop-loss check, liquidation check, may be time-consuming
     * @param newPrice
     */
    public void refreshPrice(BigDecimal newPrice) {
        
        // Not started yet
        if(!this.isStarted) return;

        // Previous task not finished
        if(!isTriggerComplete) {
            
            return;
        }
        long currentTime = Calendar.getInstance().getTimeInMillis();
        // Control to update once every 1 second+
        lastUpdateTime = currentTime;

        synchronized (this.nowPrice) {
            // If price not changed, no need to continue
            if (this.nowPrice.compareTo(newPrice) == 0) {
                
                return;
            }
            this.nowPrice = newPrice;
        }
        // Start checking orders
        isTriggerComplete = false;
        this.process(newPrice);
    }

    /**
     * Update latest trades
     * @param tradeArrayList
     */
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
//        logger.info("{} Trades update: size-{}", symbol, tradeArrayList.size());
            // Add trade details
            this.exchangePushJob.addTrades(symbol, tradeArrayList);
        }
    }

    /**
     * Process entrust orders
     * @param newPrice
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void process(BigDecimal newPrice) {
        long startTick = System.currentTimeMillis();
        this.processBlastCheck(newPrice);              // 1. Liquidation processing
        this.processOpenSpotEntrustList(newPrice);     // 2. Planned open entrust processing
        this.processCloseSpotEntrustList(newPrice);    // 3. Planned close entrust processing
        this.processCloseEntrustList(newPrice);        // 4. Close entrust processing
        this.processOpenEntrustList(newPrice);         // 5. Open entrust processing
        this.isTriggerComplete = true;
        
    }

    /**
     * Process open limit entrust orders
     * @param newPrice
     */
    public void processOpenEntrustList(BigDecimal newPrice) {
        synchronized (openOrderList) {
            Iterator<ContractOrderEntrustCoin> orderIterator = openOrderList.iterator();
            while ((orderIterator.hasNext())) {
                ContractOrderEntrustCoin order = orderIterator.next();
                if(order.getDirection() == ContractOrderDirection.BUY) { // Long
                    if(order.getEntrustPrice().compareTo(newPrice) >= 0) {
                        
                        dealOpenOrder(order);
                        orderIterator.remove();
                    }
                }else{ // Short
                    if(order.getEntrustPrice().compareTo(newPrice) <= 0) {
                        
                        dealOpenOrder(order);
                        orderIterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Process close limit entrust orders
     * @param newPrice
     */
    public void processCloseEntrustList(BigDecimal newPrice) {
        synchronized (closeOrderList) {
            Iterator<ContractOrderEntrustCoin> orderIterator = closeOrderList.iterator();
            while ((orderIterator.hasNext())) {
                ContractOrderEntrustCoin order = orderIterator.next();
                if(order.getDirection() == ContractOrderDirection.BUY){
                    if(order.getEntrustPrice().compareTo(newPrice) >= 0) {
                        
                        dealCloseOrder(order);
                        orderIterator.remove();
                    }
                }else{
                    if(order.getEntrustPrice().compareTo(newPrice) <= 0) {
                        
                        dealCloseOrder(order);
                        orderIterator.remove();
                    }
                }
            }
        }
    }

    private BigDecimal getOrderOpenFee(BigDecimal newPrice, ContractOrderEntrustCoin order) {
        return order.getVolume().multiply(contractCoin.getShareNumber()).multiply(contractCoin.getOpenFee()).divide(newPrice,8, BigDecimal.ROUND_HALF_DOWN);
    }

    /**
     * Process planned open entrust orders
     * @param newPrice
     */
    public void processOpenSpotEntrustList(BigDecimal newPrice) {
        synchronized (openOrderSpotList) {
            Iterator<ContractOrderEntrustCoin> orderIterator = openOrderSpotList.iterator();
            while ((orderIterator.hasNext())) {
                ContractOrderEntrustCoin order = orderIterator.next();
                // Two possibilities for planned entrust
                // 1. At time of entrust, trigger price > current price, now trigger price < market price => price rose to trigger => should trigger (open long = take profit, open short = stop loss)
                // 2. At time of entrust, trigger price < current price, now trigger price > market price => price fell to trigger => should trigger (open long = stop loss, open short = take profit)
                if((order.getTriggerPrice().compareTo(order.getCurrentPrice()) >= 0 && order.getTriggerPrice().compareTo(newPrice) <= 0)
                        || (order.getTriggerPrice().compareTo(order.getCurrentPrice()) <= 0 && order.getTriggerPrice().compareTo(newPrice) >= 0)) {

                    MemberContractWalletCoin wallet = memberContractWalletService.findByMemberIdAndContractCoin(order.getMemberId(), contractCoin);
                    BigDecimal leverage = order.getDirection() == ContractOrderDirection.BUY ? wallet.getCoinBuyLeverage() : wallet.getCoinSellLeverage();
                    // Check margin sufficiency
                    // 0. Calculate margin required for this open order
                    // Volume * contract size / leverage / price (for USDT margin mode)
                    BigDecimal principalAmount = order.getVolume().multiply(contractCoin.getShareNumber()).divide(leverage, 8, BigDecimal.ROUND_HALF_DOWN).divide(newPrice,8, BigDecimal.ROUND_HALF_DOWN);

                    // 1. Calculate open fee (Volume * contract size * open fee rate)
                    BigDecimal openFee = getOrderOpenFee(newPrice, order);

                    // Isolated margin mode: only compare with available balance
                    if (wallet.getCoinPattern() == ContractOrderPattern.FIXED) {
                        if (principalAmount.add(openFee).compareTo(wallet.getCoinBalance()) > 0) {
                            
                            continue;
                        }
                    }
                    // Cross margin mode: need to calculate total equity of long+short
                    if (wallet.getCoinPattern() == ContractOrderPattern.CROSSED) {
                        // Calculate total equity (long + short)
                        BigDecimal coinTotalProfitAndLoss = BigDecimal.ZERO;
                        // Long calc: (current/open - 1) * (pos+frozen) * size
                        if (wallet.getCoinBuyPrice().compareTo(BigDecimal.ZERO) > 0 && wallet.getCoinBuyPosition().compareTo(BigDecimal.ZERO) > 0) {
                            coinTotalProfitAndLoss = coinTotalProfitAndLoss.add(getBuyPL(newPrice,wallet));
                        }

                        // Short calc: (1 - current/open) * (pos+frozen) * size
                        if (wallet.getCoinSellPrice().compareTo(BigDecimal.ZERO) > 0 && wallet.getCoinSellPosition().compareTo(BigDecimal.ZERO) > 0) {
                            coinTotalProfitAndLoss = coinTotalProfitAndLoss.add(getSellPL(wallet,newPrice));
                        }

                        // Add margin
                        coinTotalProfitAndLoss = coinTotalProfitAndLoss.add(wallet.getCoinBuyPrincipalAmount()).add(wallet.getCoinSellPrincipalAmount());
                        // Result may be positive or negative, if negative subtract from balance
                        if (coinTotalProfitAndLoss.compareTo(BigDecimal.ZERO) < 0) {
                            if (principalAmount.add(openFee).compareTo(wallet.getCoinBalance().add(coinTotalProfitAndLoss)) > 0) {
                                
                                continue;
                            }
                        } else { // If equity positive, compare with balance
                            if (principalAmount.add(openFee).compareTo(wallet.getCoinBalance()) > 0) {
                                
                                continue;
                            }
                        }
                    }
                    order.setOpenFee(openFee);
                    // Trigger order
                    if(order.getEntrustPrice().compareTo(BigDecimal.ZERO) == 0) { // Market order
                        // Change type
                        order.setType(ContractOrderType.MARKET_PRICE);
                    }else{ // Limit order
                        order.setType(ContractOrderType.LIMIT_PRICE);
                    }
                    contractOrderEntrustService.saveOrUpdate(order);
                    memberContractWalletService.freezeCoinBalance(wallet, principalAmount.add(openFee));
                    try {
                        this.trade(order);
                        orderIterator.remove();
                    } catch (ParseException e) {
                        
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Process planned close entrust orders
     * @param newPrice
     */
    public void processCloseSpotEntrustList(BigDecimal newPrice) {
        
        synchronized (closeOrderSpotList) {
            Iterator<ContractOrderEntrustCoin> orderIterator = closeOrderSpotList.iterator();

            
            while ((orderIterator.hasNext())) {
                ContractOrderEntrustCoin order = orderIterator.next();
                // Two possibilities for planned entrust
                // 1. Trigger > then < current => price rose to trigger => trigger (buy close short = stop loss, sell close long = take profit)
                // 2. Trigger < then > current => price fell to trigger => trigger (buy close short = take profit, sell close long = stop loss)
                
                if ((order.getTriggerPrice().compareTo(order.getCurrentPrice()) >= 0 && order.getTriggerPrice().compareTo(newPrice) <= 0)
                        || (order.getTriggerPrice().compareTo(order.getCurrentPrice()) <= 0 && order.getTriggerPrice().compareTo(newPrice) >= 0)) {
                    
                    MemberContractWalletCoin wallet = memberContractWalletService.findByMemberIdAndContractCoin(order.getMemberId(), contractCoin);
                    // Trigger order
                    if (order.getDirection() == ContractOrderDirection.BUY) { // Buy close short, check short pos
                        // Check short position
                        if(wallet.getCoinSellPosition().compareTo(order.getVolume()) < 0) {
                            
                            continue;
                        }else{
                            // Freeze short pos
                            memberContractWalletService.freezeCoinSellPosition(wallet.getId(), order.getVolume());
                        }
                    } else { // Sell close long
                        if(wallet.getCoinBuyPosition().compareTo(order.getVolume()) < 0) {
                            
                            continue;
                        }else{
                            // Freeze long pos
                            memberContractWalletService.freezeCoinBuyPosition(wallet.getId(), order.getVolume());
                        }
                    }
                    // Trigger order
                    if(order.getEntrustPrice().compareTo(BigDecimal.ZERO) == 0) { // Market order
                        order.setType(ContractOrderType.MARKET_PRICE);
                    }else{
                        order.setType(ContractOrderType.LIMIT_PRICE);
                    }
                    // Calculate margin to deduct (close volume / (total+frozen) * total margin)
                    if (order.getDirection() == ContractOrderDirection.BUY) { // Buy close short
                        BigDecimal mPrinc = order.getVolume().divide(wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition()), 8, RoundingMode.HALF_UP)
                                .multiply(wallet.getCoinSellPrincipalAmount());
                        order.setPrincipalAmount(mPrinc);
                    } else {
                        BigDecimal mPrinc = order.getVolume().divide(wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition()), 8, RoundingMode.HALF_UP).multiply(wallet.getCoinBuyPrincipalAmount());
                        order.setPrincipalAmount(mPrinc);
                    }
                    contractOrderEntrustService.saveOrUpdate(order);
                    try {
                        this.trade(order);
                        orderIterator.remove();
                    } catch (ParseException e) {
                        
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Sync position info
     */
    public void syncMemberPosition() {
        // Sync latest user positions
        synchronized (memberContractWalletList) {
            memberContractWalletList = memberContractWalletService.findAllNeedSync(contractCoin);
            
        }
    }

    private BigDecimal getBuyCloseFee(BigDecimal newPrice, MemberContractWalletCoin wallet) {
        return wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition()).multiply(wallet.getCoinShareNumber()).multiply(contractCoin.getCloseFee()).divide(newPrice,8, BigDecimal.ROUND_DOWN);
    }

    private BigDecimal getSellCloseFee(BigDecimal newPrice, MemberContractWalletCoin wallet) {
        return wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition()).multiply(wallet.getCoinShareNumber()).multiply(contractCoin.getCloseFee()).divide(newPrice, 8, BigDecimal.ROUND_DOWN);
    }
    // Calculate liquidation price
    private MemberContractWalletCoin getForcePrice(MemberContractWalletCoin wallet){
        BigDecimal totalBuyPosition = wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition());
        BigDecimal totalSellPosition = wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition());
        // Calculate liquidation price
        if(wallet.getCoinPattern()==ContractOrderPattern.FIXED) {
            if(totalBuyPosition.compareTo(BigDecimal.ZERO)==1) {
                BigDecimal valueUsdt = totalBuyPosition.multiply(wallet.getCoinShareNumber());
                BigDecimal num = totalBuyPosition.multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinBuyPrice(), 8, BigDecimal.ROUND_HALF_DOWN);
                // Initial margin
                BigDecimal principalAmount = wallet.getCoinBuyPrincipalAmount();
                // Maintenance margin
                BigDecimal mm = num.multiply(contractCoin.getMaintenanceMarginRate());
                BigDecimal forcePrice = valueUsdt.divide(num.add(principalAmount.subtract(mm)), 8, BigDecimal.ROUND_HALF_DOWN);
                if(forcePrice.compareTo(BigDecimal.ZERO)==-1){
                    forcePrice = BigDecimal.ZERO;
                }
                wallet.setBuyForcePrice(forcePrice);
            }
            if(totalSellPosition.compareTo(BigDecimal.ZERO)==1) {
                BigDecimal valueUsdt = totalSellPosition.multiply(wallet.getCoinShareNumber());
                BigDecimal num = totalSellPosition.multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinSellPrice(), 8, BigDecimal.ROUND_HALF_DOWN);
                // Initial margin
                BigDecimal principalAmount = wallet.getCoinSellPrincipalAmount();
                // Maintenance margin
                BigDecimal mm = num.multiply(contractCoin.getMaintenanceMarginRate());
                BigDecimal forcePrice = valueUsdt.divide(num.subtract(principalAmount.subtract(mm)), 8, BigDecimal.ROUND_HALF_DOWN);

                if(forcePrice.compareTo(BigDecimal.ZERO)==-1){
                    forcePrice = BigDecimal.ZERO;
                }
                wallet.setSellForcePrice(forcePrice);
            }
        }else {
            if(totalBuyPosition.compareTo(BigDecimal.ZERO)==1) {
                BigDecimal valueUsdt = totalBuyPosition.multiply(wallet.getCoinShareNumber());
                BigDecimal num = totalBuyPosition.multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinBuyPrice(), 8, BigDecimal.ROUND_HALF_DOWN);
                // Initial margin
                BigDecimal principalAmount = wallet.getCoinBuyPrincipalAmount().add(wallet.getCoinBalance());
                // Maintenance margin
                BigDecimal mm = num.multiply(contractCoin.getMaintenanceMarginRate());
                BigDecimal forcePrice = valueUsdt.divide(num.add(principalAmount.subtract(mm)), 8, BigDecimal.ROUND_HALF_DOWN);

//                BigDecimal forcePrice = totalBuyPosition.multiply(wallet.getCoinShareNumber()).divide(totalBuyPosition.multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinBuyPrice(), 8, BigDecimal.ROUND_HALF_DOWN).subtract(contractCoin.getMaintenanceMarginRate().multiply(wallet.getCoinBuyPrincipalAmount()).multiply(wallet.getCoinBuyLeverage())).add(wallet.getCoinBuyPrincipalAmount().add(wallet.getCoinBalance())), 8, BigDecimal.ROUND_HALF_DOWN);
                if(forcePrice.compareTo(BigDecimal.ZERO)==-1){
                    forcePrice = BigDecimal.ZERO;
                }
                wallet.setBuyForcePrice(forcePrice);
            }
            if(totalSellPosition.compareTo(BigDecimal.ZERO)==1) {
                BigDecimal valueUsdt = totalSellPosition.multiply(wallet.getCoinShareNumber());
                BigDecimal num = totalSellPosition.multiply(wallet.getCoinShareNumber()).divide(wallet.getCoinSellPrice(), 8, BigDecimal.ROUND_HALF_DOWN);
                // Initial margin
                BigDecimal principalAmount = wallet.getCoinSellPrincipalAmount().add(wallet.getCoinBalance());
                // Maintenance margin
                BigDecimal mm = num.multiply(contractCoin.getMaintenanceMarginRate());
                BigDecimal forcePrice = valueUsdt.divide(num.subtract(principalAmount.subtract(mm)), 8, BigDecimal.ROUND_HALF_DOWN);
                if(forcePrice.compareTo(BigDecimal.ZERO)==-1){
                    forcePrice = BigDecimal.ZERO;
                }
                wallet.setSellForcePrice(forcePrice);
            }
        }
        return wallet;
    }

    /**
     * Liquidation check
     */
    public void processBlastCheck(BigDecimal newPrice) {
        // Sync latest user positions
        synchronized (memberContractWalletList){
            Iterator<MemberContractWalletCoin> walletIterator = memberContractWalletList.iterator();
            while(walletIterator.hasNext()) {
                MemberContractWalletCoin wallet = walletIterator.next();
                // Isolated margin
                // Check if liquidation triggered - long position
                if (wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition()).compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal forcePrice = getForcePrice(wallet).getBuyForcePrice();
                    if(forcePrice==null){
                        continue;
                    }
                    BigDecimal buyPL = newPrice.subtract(forcePrice);
                    // If margin ratio below maintenance, liquidate
                    if (buyPL.compareTo(BigDecimal.ZERO) < 0) {
                        // Liquidate long
                        blastBuy(wallet, newPrice);
                        // Update user wallet
                        MemberContractWalletCoin queryWallet = memberContractWalletService.getById(wallet.getId());
                        memberContractWalletList.set(memberContractWalletList.indexOf(wallet), queryWallet);
                    }
                }

                // Check if liquidation triggered - short position
                if (wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition()).compareTo(BigDecimal.ZERO) > 0) {
                    wallet = getForcePrice(wallet);
                    BigDecimal forcePrice = wallet.getSellForcePrice();
                    if(forcePrice==null){
                        continue;
                    }
                    BigDecimal sellPL = forcePrice.subtract(newPrice);
                    // If margin ratio below maintenance, liquidate
                    if (sellPL.compareTo(BigDecimal.ZERO) < 0) {
                        // Liquidate short
                        blastSell(wallet, newPrice);
                        // Update user wallet
                        MemberContractWalletCoin queryWallet = memberContractWalletService.getById(wallet.getId());
                        memberContractWalletList.set(memberContractWalletList.indexOf(wallet), queryWallet);
                    }
                }
            }
        }
    }

    // Liquidate long
    public void blastBuy(MemberContractWalletCoin wallet, BigDecimal price) {
        
        // Calculate PnL
        BigDecimal buyPL = getBuyPL(price,wallet);
        BigDecimal closeFee = getBuyCloseFee(price,wallet);

        // Create new entrust order
        ContractOrderEntrustCoin orderEntrust = new ContractOrderEntrustCoin();
        orderEntrust.setContractId(contractCoin.getId()); // Contract ID
        orderEntrust.setMemberId(wallet.getMemberId()); // User ID
        orderEntrust.setSymbol(contractCoin.getSymbol()); // Symbol
        orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]); // Base/settlement
        orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]); // Coin symbol
        orderEntrust.setDirection(ContractOrderDirection.SELL); // Closing direction: close long
        orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));
        orderEntrust.setVolume(wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition())); // Close volume
        orderEntrust.setTradedVolume(wallet.getCoinBuyPosition().add(wallet.getCoinFrozenBuyPosition())); // Traded volume
        orderEntrust.setTradedPrice(price); // Trade price
        orderEntrust.setPrincipalUnit(contractCoin.getSymbol().split("/")[0]); // Margin unit
        orderEntrust.setPrincipalAmount(BigDecimal.ZERO); // Margin amount
        orderEntrust.setCreateTime(DateUtil.getTimeMillis()); // Time
        orderEntrust.setType(ContractOrderType.MARKET_PRICE);
        orderEntrust.setTriggerPrice(BigDecimal.ZERO); // Trigger price
        orderEntrust.setEntrustPrice(BigDecimal.ZERO); // Entrust price
        orderEntrust.setEntrustType(ContractOrderEntrustType.CLOSE); // Close
        orderEntrust.setTriggeringTime(0L); // Trigger time, unused
        orderEntrust.setShareNumber(wallet.getCoinShareNumber());
        orderEntrust.setProfitAndLoss(buyPL); // PnL
        orderEntrust.setPatterns(wallet.getCoinPattern()); // Margin mode
        orderEntrust.setCloseFee(closeFee);
        orderEntrust.setCurrentPrice(price);
        orderEntrust.setIsBlast(1); // Is liquidation order
        orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS); // Status: success
        boolean retObj = contractOrderEntrustService.save(orderEntrust);
        if(retObj) {
            
            // Update platform profit
            contractCoinService.increaseTotalProfit(contractCoin.getId(), wallet.getCoinBuyPrincipalAmount());
            // Handle user PnL
            handlePl(wallet.getMemberId(), BigDecimal.ZERO.subtract(wallet.getCoinBuyPrincipalAmount()));
            // Update platform close fee
            contractCoinService.increaseTotalCloseFee(contractCoin.getId(), closeFee);
            // Handle fee
            handleFee(wallet.getMemberId(), closeFee);
            // Clear long positions and margin
            memberContractWalletService.blastBuy(wallet.getId());
            wallet.setCoinBuyPosition(BigDecimal.ZERO);
            wallet.setCoinFrozenBuyPosition(BigDecimal.ZERO);
            wallet.setCoinBuyPrincipalAmount(BigDecimal.ZERO);
            // Cancel all sell-close-long orders (including planned)
            List<ContractOrderEntrustCoin> closingList = contractOrderEntrustService.queryAllEntrustClosingOrdersByContractCoin(wallet.getMemberId(), contractCoin.getId(), ContractOrderDirection.SELL);
            for(ContractOrderEntrustCoin item : closingList) {
                cancelContractOrderEntrust(item, true);
            }
        }
    }

    // Liquidate short
    public void blastSell(MemberContractWalletCoin wallet, BigDecimal price) {
        
        BigDecimal sellPL = getSellPL(wallet,price);
        BigDecimal closeFee = getSellCloseFee(price,wallet);

        // Create new entrust order
        ContractOrderEntrustCoin orderEntrust = new ContractOrderEntrustCoin();
        orderEntrust.setContractId(contractCoin.getId()); // Contract ID
        orderEntrust.setMemberId(wallet.getMemberId()); // User ID
        orderEntrust.setSymbol(contractCoin.getSymbol()); // Symbol
        orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]); // Base/settlement
        orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]); // Coin symbol
        orderEntrust.setDirection(ContractOrderDirection.BUY); // Closing direction: close short
        orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));
        orderEntrust.setVolume(wallet.getCoinBuyPosition().add(wallet.getCoinFrozenSellPosition())); // Close volume
        orderEntrust.setTradedVolume(wallet.getCoinSellPosition().add(wallet.getCoinFrozenSellPosition())); // Traded volume
        orderEntrust.setTradedPrice(price); // Trade price
        orderEntrust.setPrincipalUnit(contractCoin.getSymbol().split("/")[0]); // Margin unit
        orderEntrust.setPrincipalAmount(BigDecimal.ZERO); // Margin amount
        orderEntrust.setCreateTime(DateUtil.getTimeMillis()); // Time
        orderEntrust.setType(ContractOrderType.MARKET_PRICE);
        orderEntrust.setTriggerPrice(BigDecimal.ZERO); // Trigger price
        orderEntrust.setEntrustPrice(BigDecimal.ZERO); // Entrust price
        orderEntrust.setEntrustType(ContractOrderEntrustType.CLOSE); // Close
        orderEntrust.setTriggeringTime(0L); // Trigger time, unused
        orderEntrust.setShareNumber(wallet.getCoinShareNumber());
        orderEntrust.setProfitAndLoss(sellPL); // PnL
        orderEntrust.setPatterns(wallet.getCoinPattern()); // Margin mode
        orderEntrust.setCloseFee(closeFee);
        orderEntrust.setCurrentPrice(price);
        orderEntrust.setIsBlast(1); // Is liquidation order
        orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS); // Status: success
        boolean retObj = contractOrderEntrustService.save(orderEntrust);
        if(retObj) {
            
            // Update platform profit
            contractCoinService.increaseTotalProfit(contractCoin.getId(), wallet.getCoinSellPrincipalAmount());
            // Handle user PnL
            handlePl(wallet.getMemberId(), BigDecimal.ZERO.subtract(wallet.getCoinSellPrincipalAmount()));
            // Update platform close fee
            contractCoinService.increaseTotalCloseFee(contractCoin.getId(), closeFee);
            // Handle fee
            handleFee(wallet.getMemberId(), closeFee);

            // Clear short positions and margin
            memberContractWalletService.blastSell(wallet.getId());
            wallet.setCoinSellPosition(BigDecimal.ZERO);
            wallet.setCoinFrozenSellPosition(BigDecimal.ZERO);
            wallet.setCoinSellPrincipalAmount(BigDecimal.ZERO);

            // Cancel all buy-close-short orders (including planned)
            List<ContractOrderEntrustCoin> closingList = contractOrderEntrustService.queryAllEntrustClosingOrdersByContractCoin(wallet.getMemberId(), contractCoin.getId(), ContractOrderDirection.BUY);
            for(ContractOrderEntrustCoin item : closingList) {
                cancelContractOrderEntrust(item, true);
            }
        }
    }

    // Liquidate all
    public void blastAll(MemberContractWalletCoin wallet, BigDecimal price) {
        

        // Close long
        blastBuy(wallet, price);

        // Close short
        blastSell(wallet, price);

        // Clear balance
        memberContractWalletService.decreaseCoinBalance(wallet.getId(), wallet.getCoinBalance());

        // Update platform profit
        contractCoinService.increaseTotalProfit(contractCoin.getId(), wallet.getCoinBalance());

        // Handle user PnL (all lost)
        handlePl(wallet.getMemberId(), BigDecimal.ZERO.subtract(wallet.getCoinBalance()));
    }

    // Cancel order (entrust)
    public synchronized void cancelContractOrderEntrust(ContractOrderEntrustCoin orderEntrust, boolean isBlast) {
        // Find order
        LinkedList<ContractOrderEntrustCoin> list = null;
        if(orderEntrust.getEntrustType() == ContractOrderEntrustType.OPEN) {
            if(orderEntrust.getType() == ContractOrderType.SPOT_LIMIT) {
                list = this.openOrderSpotList;
                
            }else{
                
                list = this.openOrderList;
            }
        }else{
            if(orderEntrust.getType() == ContractOrderType.SPOT_LIMIT) {
                list = this.closeOrderSpotList;
                
            }else{
                list = this.closeOrderList;
                
            }
        }
        synchronized (list) {
            
            Iterator<ContractOrderEntrustCoin> orderIterator = list.iterator();
            while ((orderIterator.hasNext())) {
                ContractOrderEntrustCoin order = orderIterator.next();
                
                if (order.getId().longValue() == orderEntrust.getId().longValue()) {
                    
                    MemberContractWalletCoin wallet = memberContractWalletService.findByMemberIdAndContractCoin(orderEntrust.getMemberId(), contractCoin);
                    // Update DB
                    if(orderEntrust.getEntrustType() == ContractOrderEntrustType.OPEN) {
                        // Open order, need to unfreeze USDT
                        if(orderEntrust.getType() == ContractOrderType.LIMIT_PRICE || orderEntrust.getType() == ContractOrderType.MARKET_PRICE) {
                            // Limit/market order, unfreeze margin unless liquidation
                            if(!isBlast) {
                                memberContractWalletService.thawCoinBalance(wallet, orderEntrust.getPrincipalAmount().add(orderEntrust.getOpenFee()));
                            }
                        }else{
                            // Planned order, no frozen assets
                        }
                    }else{
                        if(orderEntrust.getType() == ContractOrderType.LIMIT_PRICE || orderEntrust.getType() == ContractOrderType.MARKET_PRICE) {
                            // Close order, need to unfreeze position
                            if (orderEntrust.getDirection() == ContractOrderDirection.BUY) { // Buy close short
                                if(!isBlast) {
                                    memberContractWalletService.thrawCoinSellPosition(wallet.getId(), orderEntrust.getVolume());
                                }
                            } else {
                                if(!isBlast) {
                                    memberContractWalletService.thrawCoinBuyPosition(wallet.getId(), orderEntrust.getVolume());
                                }
                            }
                        }else{
                            // Planned order, no frozen assets
                        }
                    }
                    contractOrderEntrustService.updateStatus(orderEntrust.getId(), ContractOrderEntrustStatus.ENTRUST_CANCEL);
                    orderIterator.remove();
                }
            }
        }
    }

    /**
     * Initialize Thumb
     */
    public void initializeThumb() {
        this.thumb = new CoinThumb();
        this.thumb.setChg(BigDecimal.ZERO);                 // Change percentage (e.g. 4%)
        this.thumb.setChange(BigDecimal.ZERO);              // Change amount
        this.thumb.setOpen(BigDecimal.ZERO);                // Open price
        this.thumb.setClose(BigDecimal.ZERO);               // Close price
        this.thumb.setHigh(BigDecimal.ZERO);                // High price
        this.thumb.setLow(BigDecimal.ZERO);                 // Low price
        this.thumb.setBaseUsdRate(BigDecimal.valueOf(7.0)); // Base USDT rate
        this.thumb.setLastDayClose(BigDecimal.ZERO);        // Previous day close
        this.thumb.setSymbol(this.symbol);                  // Trading pair symbol
        this.thumb.setUsdRate(BigDecimal.valueOf(7.0));     // USDT rate
        this.thumb.setZone(0);                              // Trading zone
        this.thumb.setVolume(BigDecimal.ZERO);              // Volume
        this.thumb.setTurnover(BigDecimal.ZERO);            // Turnover
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
    // Get trading pair symbol
    public String getSymbol() { return this.symbol; }
    // Get coin symbol
    public String getCoinSymbol() { return this.coinSymbol; }
    // Get base symbol
    public String getBaseSymbol() { return this.baseSymbol; }
    // Get latest price
    public BigDecimal getNowPrice() { return this.nowPrice; }
    // Get latest market data
    public CoinThumb getThumb() { return this.thumb; }
    // Get latest trades
    public List<ContractTrade> getLastedTradeList() { return this.lastedTradeList; }
    // Get order book
    public TradePlate getTradePlate(ContractOrderDirection direction){
        if(direction == ContractOrderDirection.BUY){
            return buyTradePlate;
        }
        else{
            return sellTradePlate;
        }
    }
    // Set contract coin service
    public void setContractCoinService(ContractCoinCoinService contractCoinService) { this.contractCoinService = contractCoinService; }
    // Set entrust service
    public void setContractOrderEntrustService(ContractOrderEntrustCoinService contractOrderEntrustService){ this.contractOrderEntrustService = contractOrderEntrustService; }
    // Add handler
    public void addHandler(MarketHandler storage) {
        handlers.add(storage);
    }
    public void setExchangePushJob(ExchangePushJob job) { this.exchangePushJob = job; }

    public void setMemberTransactionFeign(MemberTransactionFeign memberTransactionFeign) {
        this.memberTransactionFeign = memberTransactionFeign;
    }

    public void setSnowflakeConfig(SnowflakeConfig snowflakeConfig) { this.snowflakeConfig = snowflakeConfig; }

    public void setMemberContractWalletService(MemberContractWalletCoinService memberContractWalletService) {
        this.memberContractWalletService = memberContractWalletService;
    }

    /**
     * Update contract coin info
     * @param coin
     */
    public void updateContractCoin(ContractCoinCoin coin) {
        synchronized (contractCoin) {
            contractCoin = coin;
        }
    }

    /**
     * Fixed-price liquidation
     * @param newPrice
     */
    public void refreshBlastPrice(BigDecimal newPrice) {
        
        // Not started
        if(!this.isStarted) return;
        this.process(newPrice);
    }

    /**
     * Get all user positions in engine
     * @return
     */
    public List<MemberContractWalletCoin> getMemberContractWalletList() {
        return this.memberContractWalletList;
    }

    /**
     * Handle fee
     */
    public void handleFee(Long memberId, BigDecimal fee){
        
    }

    /**
     * Handle profit/loss
     */
    public void handlePl(Long memberId, BigDecimal pL){
        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(pL);
        memberTransaction.setMemberId(memberId);
        memberTransaction.setSymbol(contractCoin.getSymbol().split("/")[0]);
        memberTransaction.setType(pL.compareTo(BigDecimal.ZERO) > 0 ? TransactionType.CONTRACT_PROFIT.getCode() : TransactionType.CONTRACT_LOSS.getCode());
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransactionFeign.save(memberTransaction);

        
    }

    /**
     * Update wallet
     * @param walletId
     */
    public void memberWalletChange(Long walletId) {
        synchronized (memberContractWalletList) {
            boolean hasWallet = false;
            Iterator<MemberContractWalletCoin> walletIterator = memberContractWalletList.iterator();
            while(walletIterator.hasNext()) {
                MemberContractWalletCoin wallet = walletIterator.next();
                if(wallet.getId().longValue() == walletId.longValue()) {
                    hasWallet = true;
                    
                    // Update wallet
                    MemberContractWalletCoin queryResult = memberContractWalletService.getById(walletId);
                    memberContractWalletList.set(memberContractWalletList.indexOf(wallet), queryResult);
                    break;
                }
            }
            // If not found, add new
            if(!hasWallet) {
                MemberContractWalletCoin wallet = memberContractWalletService.getById(walletId);
                if(wallet != null) {
                    
                    memberContractWalletList.add(wallet);
                }
            }
        }
    }
}

