package com.wikex.wikex.swap.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wikex.wikex.config.SnowflakeConfig;
import com.wikex.wikex.constant.*;
import com.wikex.wikex.pojo.*;
import com.wikex.wikex.swap.entity.*;
import com.wikex.wikex.swap.handler.MarketHandler;
import com.wikex.wikex.swap.job.ExchangePushJob;
import com.wikex.wikex.swap.service.*;
import com.wikex.wikex.user.entity.MemberTransaction;
import com.wikex.wikex.user.feign.MemberTransactionFeign;
import com.wikex.wikex.util.DateUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class ContractCoinMatch {

    private String symbol;
    private String baseSymbol;
    private String coinSymbol;
    private CoinThumb thumb;
    private LinkedList<ContractTrade> lastedTradeList;
    private int lastedTradeListSize = 50;

    private long lastUpdateTime = 0L;
    private boolean isTriggerComplete = true;
    private BigDecimal nowPrice = BigDecimal.ZERO;

    private ContractCoinService contractCoinService;
    private ContractOrderEntrustService contractOrderEntrustService;

    private MemberContractPositionService memberContractPositionService;
    private MemberTransactionFeign memberTransactionFeign;
    private MemberContractWalletService memberContractWalletService;
    private MemberTradeLimitService memberTradeLimitService;
    private ContractCoin contractCoin;

    private SnowflakeConfig snowflakeConfig;

    private List<ContractOrderEntrust> contractOrderEntrustList = new ArrayList<>();
    private List<MemberContractWallet> memberContractWalletList = new ArrayList<>();

    private List<MarketHandler> handlers;
    private ExchangePushJob exchangePushJob;

    private TradePlate sellTradePlate;

    private TradePlate buyTradePlate;

    private boolean isStarted = false;

    private LinkedList<ContractOrderEntrust> openOrderList = new LinkedList<ContractOrderEntrust>();
    private LinkedList<ContractOrderEntrust> closeOrderList = new LinkedList<ContractOrderEntrust>();

    private LinkedList<ContractOrderEntrust> openOrderSpotList = new LinkedList<ContractOrderEntrust>();
    private LinkedList<ContractOrderEntrust> closeOrderSpotList = new LinkedList<ContractOrderEntrust>();

    
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

    public void trade(ContractOrderEntrust order) throws ParseException {
        if (!this.isStarted)
            return;
        if (!symbol.equalsIgnoreCase(order.getSymbol())) {

            return;
        }
        if (order.getPrincipalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (order.getEntrustType() == ContractOrderEntrustType.OPEN) {

            if (order.getType() == ContractOrderType.MARKET_PRICE) {
                this.dealOpenOrder(order);
            } else if (order.getType() == ContractOrderType.LIMIT_PRICE) {

                if (order.getDirection() == ContractOrderDirection.BUY
                        && order.getEntrustPrice().compareTo(nowPrice) >= 0
                        || order.getDirection() == ContractOrderDirection.SELL
                                && order.getEntrustPrice().compareTo(nowPrice) <= 0) {
                    this.dealOpenOrder(order);
                } else {

                    synchronized (openOrderList) {

                        openOrderList.addLast(order);
                    }
                }
            } else if (order.getType() == ContractOrderType.SPOT_LIMIT) {
                synchronized (openOrderSpotList) {

                    openOrderSpotList.add(order);
                }
            }
        } else if (order.getEntrustType().equals(ContractOrderEntrustType.CLOSE)) {
            if (order.getType().equals(ContractOrderType.MARKET_PRICE)) {
                this.dealCloseOrder(order);
            } else if (order.getType().equals(ContractOrderType.LIMIT_PRICE)) {
                if (order.getDirection().equals(ContractOrderDirection.BUY)
                        && order.getEntrustPrice().compareTo(nowPrice) > 0
                        || order.getDirection().equals(ContractOrderDirection.SELL)
                                && order.getEntrustPrice().compareTo(nowPrice) < 0) {
                    this.dealCloseOrder(order);
                } else {

                    synchronized (closeOrderList) {

                        closeOrderList.addLast(order);
                    }
                }
            } else if (order.getType().equals(ContractOrderType.SPOT_LIMIT)) {
                synchronized (closeOrderSpotList) {

                    closeOrderSpotList.add(order);
                }
            }
        }
    }

    
    @Transactional(rollbackFor = Exception.class)
    public void dealOpenOrder(ContractOrderEntrust order) {

        ContractOrderEntrust orderOld = contractOrderEntrustService.getById(order.getId());
        if (!orderOld.getStatus().equals(ContractOrderEntrustStatus.ENTRUST_ING)) {

            return;
        }

        MemberContractWallet memberContractWallet = memberContractWalletService.findByMemberId(order.getMemberId());

        memberContractWalletService.decreaseUsdtFrozen(memberContractWallet.getId(), order.getOpenFee());

        contractCoinService.increaseTotalOpenFee(contractCoin.getId(), order.getOpenFee());

        handleFee(order.getMemberId(), order.getOpenFee());

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(BigDecimal.ZERO.subtract(order.getOpenFee()));
        memberTransaction.setMemberId(order.getMemberId());
        memberTransaction.setSymbol(contractCoin.getSymbol().split("/")[1]);
        memberTransaction.setType(TransactionType.CONTRACT_FEE.getCode());
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransactionFeign.save(memberTransaction);

        memberContractWalletService.decreaseUsdtFrozen(memberContractWallet.getId(), order.getPrincipalAmount());

        BigDecimal openPrice = order.getCurrentPrice();
        BigDecimal leverage = order.getLeverage();
        if (leverage == null) {
            if (order.getDirection().equals(ContractOrderDirection.BUY)) {
                leverage = memberContractWallet.getUsdtBuyLeverage();
            } else {
                leverage = memberContractWallet.getUsdtSellLeverage();
            }
        }

        LambdaQueryWrapper<MemberContractPosition> query = new LambdaQueryWrapper<>();
        query.eq(MemberContractPosition::getMemberId, order.getMemberId());
        query.eq(MemberContractPosition::getContractId, contractCoin.getId());
        query.eq(MemberContractPosition::getDirection, order.getDirection());
        List<MemberContractPosition> list = memberContractPositionService.list(query);
        MemberContractPosition position = null;
        if (list != null && list.size() > 0) {
            position = list.get(0);
        } else {

            position = new MemberContractPosition();
            position.setContractId(contractCoin.getId());
            position.setFrozenPrincipalAmount(BigDecimal.ZERO);
            position.setPrincipalAmount(BigDecimal.ZERO);
            position.setDirection(order.getDirection());
            position.setMemberId(order.getMemberId());
            position.setShareNumber(contractCoin.getShareNumber());
            position.setFrozenPosition(BigDecimal.ZERO);
            position.setPosition(BigDecimal.ZERO);
            position.setPrice(BigDecimal.ZERO);
        }
        position.setPrincipalAmount(position.getPrincipalAmount().add(order.getPrincipalAmount()));

        BigDecimal oldValue = position.getPosition().multiply(position.getShareNumber()).multiply(position.getPrice());
        BigDecimal newValue = order.getVolume().multiply(position.getShareNumber()).multiply(openPrice);
        BigDecimal totalNum = position.getPosition().add(order.getVolume()).multiply(position.getShareNumber());
        BigDecimal price = (oldValue.add(newValue)).divide(totalNum, 8, RoundingMode.DOWN);
        position.setPrice(price);
        position.setPattern(memberContractWallet.getUsdtPattern().getCode());
        position.setLeverage(leverage);
        position.setPosition(position.getPosition().add(order.getVolume()));

        if (memberContractWallet.getUsdtPattern().equals(ContractOrderPattern.CROSSED)) {

        } else {

            BigDecimal forcePrice = computeForcePriceFixed(position);
            position.setForcePrice(forcePrice);
        }

        memberContractPositionService.saveOrUpdate(position);

        if (memberContractWallet.getUsdtShareNumber().compareTo(order.getShareNumber()) != 0) {
            memberContractWalletService.updateShareNumber(memberContractWallet.getId(), order.getShareNumber());
        }
        order.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS);
        order.setTradedPrice(openPrice);
        order.setPositionId(position.getId());
        contractOrderEntrustService.updateById(order);
    }

    
    private BigDecimal computeForcePriceFixed(MemberContractPosition position) {
        BigDecimal closeFee = position.getPosition().multiply(position.getShareNumber()).multiply(position.getPrice())
                .multiply(contractCoin.getCloseFee());

        BigDecimal forcePrice;

        BigDecimal value = position.getPosition().multiply(position.getShareNumber()).multiply(position.getPrice());
        BigDecimal rate = position.getPrincipalAmount().divide(value, 8, BigDecimal.ROUND_DOWN);

        if (position.getDirection().equals(ContractOrderDirection.BUY)) {

            forcePrice = position.getPrice()
                    .multiply(BigDecimal.ONE.subtract(rate).add(contractCoin.getMaintenanceMarginRate()))
                    .add(closeFee.divide(position.getShareNumber().multiply(position.getPosition()), 8,
                            BigDecimal.ROUND_DOWN));

        } else {

            forcePrice = position.getPrice()
                    .multiply(BigDecimal.ONE.add(rate).subtract(contractCoin.getMaintenanceMarginRate()))
                    .add(closeFee.divide(position.getShareNumber().multiply(position.getPosition()), 8,
                            BigDecimal.ROUND_DOWN));

        }

        if (forcePrice.compareTo(BigDecimal.ZERO) == -1) {
            forcePrice = BigDecimal.ZERO;
        }
        return forcePrice;
    }

    public static void main(String[] args) {

        BigDecimal price = new BigDecimal("3587.21000000");
        BigDecimal ableBalance = new BigDecimal("-84700.7809700000000000");
        BigDecimal m = new BigDecimal("356.927395000000000000000000");
        BigDecimal lastNum = new BigDecimal("1.00000000");
        BigDecimal newPrice = new BigDecimal("3588.1");

        BigDecimal forcePrice = price.subtract((ableBalance.add(m)).divide(lastNum, 8, BigDecimal.ROUND_DOWN));

    }

    
    @Transactional(rollbackFor = Exception.class)
    public void dealCloseOrder(ContractOrderEntrust order) {

        ContractOrderEntrust orderOld = contractOrderEntrustService.getById(order.getId());
        if (!orderOld.getStatus().equals(ContractOrderEntrustStatus.ENTRUST_ING)) {

            return;
        }

        MemberContractWallet memberContractWallet = memberContractWalletService.findByMemberId(order.getMemberId());
        MemberContractPosition position = memberContractPositionService.getById(order.getPositionId());
        if (position == null) {
            log.error(" orderId::" + order.getId());
            return;
        }

        BigDecimal spread = contractCoin.getSpread();
        Integer spreadType = contractCoin.getSpreadType();

        MemberTradeLimit limit = memberTradeLimitService.findLimitByMemberIdAndContractId(order.getMemberId(),
                contractCoin.getId());
        if (limit != null) {

            if (limit.getSpreadType() != null) {
                spreadType = limit.getSpreadType();
            }
            if (limit.getSpread() != null && limit.getSpread().compareTo(BigDecimal.ZERO) > 0) {
                spread = limit.getSpread();
            }
        }

        BigDecimal dealPrice = nowPrice;
        if (order.getType() == ContractOrderType.MARKET_PRICE) {
            if (order.getDirection() == ContractOrderDirection.BUY) {
                if (spreadType == 1) {
                    dealPrice = nowPrice.add(nowPrice.multiply(spread));
                } else {
                    dealPrice = nowPrice.add(spread);
                }
            } else {
                if (spreadType == 1) {
                    dealPrice = nowPrice.subtract(nowPrice.multiply(spread));
                } else {
                    dealPrice = nowPrice.subtract(spread);
                }
            }
        }

        position.setPosition(position.getPosition().subtract(order.getVolume()));
        position.setFrozenPosition(position.getFrozenPosition().subtract(order.getVolume()));
        position.setPrincipalAmount(position.getPrincipalAmount().subtract(order.getPrincipalAmount()));
        position.setFrozenPrincipalAmount(position.getFrozenPrincipalAmount().subtract(order.getPrincipalAmount()));

        if (order.getDirection() == ContractOrderDirection.BUY) {

            BigDecimal pL = position.getPrice().subtract(dealPrice).multiply(order.getVolume())
                    .multiply(contractCoin.getShareNumber());

            BigDecimal profit = position.getProfit();
            if (profit == null) {
                profit = BigDecimal.ZERO;
            }
            position.setProfit(profit.add(pL));

            BigDecimal principalAmount = order.getPrincipalAmount();

            BigDecimal closeFee = order.getPrincipalAmount().multiply(position.getLeverage())
                    .multiply(contractCoin.getCloseFee());

            memberContractWalletService.increaseUsdtBalance(memberContractWallet.getId(),
                    principalAmount.add(pL).subtract(closeFee));

            contractCoinService.increaseTotalCloseFee(contractCoin.getId(), closeFee);

            handleFee(memberContractWallet.getMemberId(), closeFee);

            order.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS);
            order.setTradedPrice(dealPrice);
            order.setProfitAndLoss(pL);
            order.setCloseFee(closeFee);
            contractOrderEntrustService.updateById(order);

            if (pL.compareTo(BigDecimal.ZERO) > 0) {
                memberContractWalletService.increaseUsdtProfit(memberContractWallet.getId(), pL);
                contractCoinService.increaseTotalLoss(contractCoin.getId(), pL);
            }
            if (pL.compareTo(BigDecimal.ZERO) < 0) {
                memberContractWalletService.increaseUsdtLoss(memberContractWallet.getId(),
                        BigDecimal.ZERO.subtract(pL));
                contractCoinService.increaseTotalProfit(contractCoin.getId(), BigDecimal.ZERO.subtract(pL));
            }

            handlePl(memberContractWallet.getMemberId(), pL);

        } else {

            BigDecimal pL = dealPrice.subtract(position.getPrice()).multiply(order.getVolume())
                    .multiply(contractCoin.getShareNumber());

            BigDecimal profit = position.getProfit();
            if (profit == null) {
                profit = BigDecimal.ZERO;
            }
            position.setProfit(profit.add(pL));

            BigDecimal principalAmount = order.getPrincipalAmount();

            BigDecimal closeFee = order.getPrincipalAmount().multiply(position.getLeverage())
                    .multiply(contractCoin.getCloseFee());

            memberContractWalletService.increaseUsdtBalance(memberContractWallet.getId(),
                    principalAmount.add(pL).subtract(closeFee));

            contractCoinService.increaseTotalCloseFee(contractCoin.getId(), closeFee);

            handleFee(memberContractWallet.getMemberId(), closeFee);

            if (memberContractWallet.getUsdtShareNumber().compareTo(order.getShareNumber()) != 0) {
                memberContractWalletService.updateShareNumber(memberContractWallet.getId(), order.getShareNumber());
            }

            order.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS);
            order.setTradedPrice(dealPrice);
            order.setCloseFee(closeFee);
            order.setProfitAndLoss(pL);
            contractOrderEntrustService.saveOrUpdate(order);

            if (pL.compareTo(BigDecimal.ZERO) > 0) {
                memberContractWalletService.increaseUsdtProfit(memberContractWallet.getId(), pL);
                contractCoinService.increaseTotalLoss(contractCoin.getId(), pL);
            }
            if (pL.compareTo(BigDecimal.ZERO) < 0) {
                memberContractWalletService.increaseUsdtLoss(memberContractWallet.getId(),
                        BigDecimal.ZERO.subtract(pL));
                contractCoinService.increaseTotalProfit(contractCoin.getId(), BigDecimal.ZERO.subtract(pL));
            }

            handlePl(memberContractWallet.getMemberId(), pL);
        }

        memberContractPositionService.updateById(position);

    }

    
    public void run() {

        contractCoin = contractCoinService.findBySymbol(symbol);
        if (contractCoin == null) {

            return;
        }

        contractOrderEntrustList = contractOrderEntrustService.loadUnMatchOrders(contractCoin.getId());
        if (contractOrderEntrustList != null && contractOrderEntrustList.size() > 0) {

            for (ContractOrderEntrust item : contractOrderEntrustList) {
                if (item.getEntrustType() == ContractOrderEntrustType.OPEN) {
                    if (item.getType() == ContractOrderType.SPOT_LIMIT) {

                        openOrderSpotList.add(item);
                    } else {
                        openOrderList.add(item);
                    }
                } else {
                    if (item.getType() == ContractOrderType.SPOT_LIMIT) {
                        closeOrderSpotList.add(item);
                    } else {
                        closeOrderList.add(item);
                    }
                }
            }
        }

        this.syncMemberPosition();
        this.isStarted = true;
    }

    
    public void refreshPlate(List<TradePlateItem> buyPlateItems, List<TradePlateItem> sellPlateItems) {
        if (!this.isStarted)
            return;
        if (buyPlateItems.size() > 0) {
            this.buyTradePlate.setItems(buyPlateItems);
            this.exchangePushJob.addPlates(symbol, buyTradePlate);
        }
        if (sellPlateItems.size() > 0) {
            this.sellTradePlate.setItems(sellPlateItems);
            this.exchangePushJob.addPlates(symbol, sellTradePlate);
        }

    }

    
    public void refreshThumb(CoinThumb thumb) {
        if (!this.isStarted)
            return;

        this.thumb.setHigh(thumb.getHigh());
        this.thumb.setLow(thumb.getLow());
        this.thumb.setOpen(thumb.getClose());
        this.thumb.setClose(thumb.getClose());
        this.thumb.setTurnover(thumb.getTurnover());
        this.thumb.setVolume(thumb.getVolume());
        this.thumb.setUsdRate(thumb.getClose());

        this.thumb.setChange(thumb.getClose().subtract(thumb.getOpen()));
        if (thumb.getOpen().compareTo(BigDecimal.ZERO) > 0) {
            this.thumb.setChg(this.thumb.getChange().divide(this.thumb.getOpen(), 4, RoundingMode.UP));
        }

        handleCoinThumb();
    }

    
    public void refreshPrice(Map<String, BigDecimal> mapPrices) {
        BigDecimal newPrice = mapPrices.get(this.symbol);

        if (!this.isStarted)
            return;

        if (!isTriggerComplete) {

            return;
        } else {

        }
        long currentTime = Calendar.getInstance().getTimeInMillis();

        lastUpdateTime = currentTime;

        synchronized (this.nowPrice) {

            if (this.nowPrice.compareTo(newPrice) == 0) {
                isTriggerComplete = true;

                return;
            }
            this.nowPrice = newPrice;
        }

        isTriggerComplete = false;
        try {
            this.process(mapPrices);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.isTriggerComplete = true;
        }

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

    
    @GlobalTransactional(rollbackFor = Exception.class)
    public void process(Map<String, BigDecimal> mapPrices) {
        BigDecimal newPrice = mapPrices.get(this.symbol);
        long startTick = System.currentTimeMillis();

        this.processBlastCheck(mapPrices);

        this.processCloseSpotEntrustList(newPrice);
        this.processOpenSpotEntrustList(newPrice);
        this.processCloseEntrustList(newPrice);
        this.processOpenEntrustList(newPrice);

    }

    
    public void processOpenEntrustList(BigDecimal newPrice) {
        synchronized (openOrderList) {
            Iterator<ContractOrderEntrust> orderIterator = openOrderList.iterator();
            while ((orderIterator.hasNext())) {
                ContractOrderEntrust order = orderIterator.next();
                if (order.getDirection() == ContractOrderDirection.BUY) {
                    if (order.getEntrustPrice().compareTo(newPrice) >= 0) {

                        dealOpenOrder(order);
                        orderIterator.remove();
                    }
                } else {
                    if (order.getEntrustPrice().compareTo(newPrice) <= 0) {

                        dealOpenOrder(order);
                        orderIterator.remove();
                    }
                }
            }
        }
    }

    
    public void processCloseEntrustList(BigDecimal newPrice) {
        synchronized (closeOrderList) {
            Iterator<ContractOrderEntrust> orderIterator = closeOrderList.iterator();
            while ((orderIterator.hasNext())) {
                ContractOrderEntrust order = orderIterator.next();
                if (order.getDirection() == ContractOrderDirection.BUY) {
                    if (order.getEntrustPrice().compareTo(newPrice) >= 0) {

                        dealCloseOrder(order);
                        orderIterator.remove();
                    }
                } else {
                    if (order.getEntrustPrice().compareTo(newPrice) <= 0) {

                        dealCloseOrder(order);
                        orderIterator.remove();
                    }
                }
            }
        }
    }

    
    @Transactional(rollbackFor = Exception.class)
    public void processCloseSpotEntrustList(BigDecimal newPrice) {

        synchronized (closeOrderSpotList) {
            Iterator<ContractOrderEntrust> orderIterator = closeOrderSpotList.iterator();

            while ((orderIterator.hasNext())) {
                ContractOrderEntrust order = orderIterator.next();

                if ((order.getTriggerPrice().compareTo(order.getCurrentPrice()) >= 0
                        && order.getTriggerPrice().compareTo(newPrice) <= 0)
                        || (order.getTriggerPrice().compareTo(order.getCurrentPrice()) <= 0
                                && order.getTriggerPrice().compareTo(newPrice) >= 0)) {

                    if (order.getEntrustPrice().compareTo(BigDecimal.ZERO) == 0) {
                        order.setType(ContractOrderType.MARKET_PRICE);
                    } else {
                        order.setType(ContractOrderType.LIMIT_PRICE);
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

    
    public void processOpenSpotEntrustList(BigDecimal newPrice) {

        synchronized (openOrderSpotList) {

            Iterator<ContractOrderEntrust> orderIterator = openOrderSpotList.iterator();
            while ((orderIterator.hasNext())) {
                ContractOrderEntrust order = orderIterator.next();

                if ((order.getTriggerPrice().compareTo(order.getCurrentPrice()) >= 0
                        && order.getTriggerPrice().compareTo(newPrice) <= 0)
                        || (order.getTriggerPrice().compareTo(order.getCurrentPrice()) <= 0
                                && order.getTriggerPrice().compareTo(newPrice) >= 0)) {

                    MemberContractWallet wallet = memberContractWalletService.findByMemberId(order.getMemberId());
                    BigDecimal leverage = order.getDirection() == ContractOrderDirection.BUY
                            ? wallet.getUsdtBuyLeverage()
                            : wallet.getUsdtSellLeverage();

                    BigDecimal principalAmount = order.getVolume().multiply(contractCoin.getShareNumber())
                            .multiply(newPrice).divide(leverage, 8, BigDecimal.ROUND_HALF_DOWN);

                    BigDecimal openFee = order.getVolume().multiply(contractCoin.getShareNumber()).multiply(newPrice)
                            .multiply(contractCoin.getOpenFee());

                    if (wallet.getUsdtPattern() == ContractOrderPattern.FIXED) {
                        if (principalAmount.add(openFee).compareTo(wallet.getUsdtBalance()) > 0) {

                            continue;
                        }
                    }

                    if (wallet.getUsdtPattern() == ContractOrderPattern.CROSSED) {

                        BigDecimal usdtTotalProfitAndLoss = BigDecimal.ZERO;

                        if (wallet.getUsdtBuyPrice().compareTo(BigDecimal.ZERO) > 0
                                && wallet.getUsdtBuyPosition().compareTo(BigDecimal.ZERO) > 0) {
                            usdtTotalProfitAndLoss = usdtTotalProfitAndLoss
                                    .add(newPrice.subtract(wallet.getUsdtBuyPrice())
                                            .multiply(
                                                    wallet.getUsdtBuyPosition().add(wallet.getUsdtFrozenBuyPosition()))
                                            .multiply(wallet.getUsdtShareNumber()));
                        }

                        if (wallet.getUsdtSellPrice().compareTo(BigDecimal.ZERO) > 0
                                && wallet.getUsdtSellPosition().compareTo(BigDecimal.ZERO) > 0) {
                            usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(wallet.getUsdtSellPrice()
                                    .subtract(newPrice)
                                    .multiply(wallet.getUsdtSellPosition().add(wallet.getUsdtFrozenSellPosition()))
                                    .multiply(wallet.getUsdtShareNumber()));
                        }

                        usdtTotalProfitAndLoss = usdtTotalProfitAndLoss.add(wallet.getUsdtBuyPrincipalAmount());

                        if (usdtTotalProfitAndLoss.compareTo(BigDecimal.ZERO) < 0) {
                            if (principalAmount.add(openFee)
                                    .compareTo(wallet.getUsdtBalance().add(usdtTotalProfitAndLoss)) > 0) {

                                continue;
                            }
                        } else {
                            if (principalAmount.add(openFee).compareTo(wallet.getUsdtBalance()) > 0) {

                                continue;
                            }
                        }
                    }
                    order.setOpenFee(openFee);

                    if (order.getEntrustPrice().compareTo(BigDecimal.ZERO) == 0) {

                        order.setType(ContractOrderType.MARKET_PRICE);
                    } else {

                        order.setType(ContractOrderType.LIMIT_PRICE);
                    }
                    contractOrderEntrustService.saveOrUpdate(order);
                    memberContractWalletService.freezeUsdtBalance(wallet, principalAmount.add(openFee));
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

    
    public void syncMemberPosition() {

        synchronized (memberContractWalletList) {
            memberContractWalletList = memberContractWalletService.findAllNeedSync(contractCoin);

        }
    }

    
    @Transactional(rollbackFor = Exception.class)
    public void processBlastCheck(Map<String, BigDecimal> mapPrice) {

        try {

            synchronized (memberContractWalletList) {
                List<Long> memberIds = memberContractPositionService
                        .queryMemberIdsHoldingPositions(contractCoin.getId());
                if (memberIds == null || memberIds.size() == 0) {
                    return;
                }

                for (int i = 0; i < memberIds.size(); i++) {
                    Long memberId = memberIds.get(i);

                    MemberContractWallet wallet = memberContractWalletService.findByMemberId(memberId);
                    if (wallet == null) {
                        continue;
                    }
                    if (ContractOrderPattern.FIXED.equals(wallet.getUsdtPattern())) {
                        BigDecimal newPrice = mapPrice.get(this.symbol);

                        LambdaQueryWrapper<MemberContractPosition> queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(MemberContractPosition::getMemberId, wallet.getMemberId());
                        queryWrapper.eq(MemberContractPosition::getContractId, contractCoin.getId());
                        queryWrapper.eq(MemberContractPosition::getDirection, ContractOrderDirection.BUY);
                        queryWrapper.gt(MemberContractPosition::getPrincipalAmount, BigDecimal.ZERO);
                        queryWrapper.ge(MemberContractPosition::getForcePrice, newPrice);
                        List<MemberContractPosition> buyList = memberContractPositionService.list(queryWrapper);

                        queryWrapper = new LambdaQueryWrapper<>();
                        queryWrapper.eq(MemberContractPosition::getMemberId, wallet.getMemberId());
                        queryWrapper.eq(MemberContractPosition::getContractId, contractCoin.getId());
                        queryWrapper.eq(MemberContractPosition::getDirection, ContractOrderDirection.SELL);
                        queryWrapper.gt(MemberContractPosition::getPrincipalAmount, BigDecimal.ZERO);
                        queryWrapper.le(MemberContractPosition::getForcePrice, newPrice);
                        List<MemberContractPosition> sellList = memberContractPositionService.list(queryWrapper);

                        if (buyList != null && buyList.size() > 0) {

                            for (MemberContractPosition position : buyList) {

                                blastBuy(wallet, position, position.getForcePrice(), contractCoin);

                            }
                        }

                        if (sellList != null && sellList.size() > 0) {

                            for (MemberContractPosition position : sellList) {

                                blastSell(wallet, position, position.getForcePrice(), contractCoin);

                            }
                        }
                    } else {

                        List<MemberContractPosition> list = memberContractPositionService
                                .queryAllHoldingPositions(memberId);
                        if (list == null || list.size() == 0) {
                            continue;
                        }

                        Map<String, List<MemberContractPosition>> listMap = list.stream()
                                .collect(Collectors.groupingBy(MemberContractPosition::getSymbol));

                        BigDecimal ableBalance = wallet.getUsdtBalance();
                        Map<String, BigDecimal> mmMap = new HashMap<>();
                        Map<String, String> pdMap = new HashMap<>();
                        Map<String, BigDecimal> lastMap = new HashMap<>();
                        Map<String, BigDecimal> prMap = new HashMap<>();

                        Set<String> symbols = listMap.keySet();
                        BigDecimal ks = BigDecimal.ZERO;
                        for (String sy : symbols) {
                            BigDecimal newPrice = mapPrice.get(sy);
                            List<MemberContractPosition> positions = listMap.get(sy);
                            if (positions != null && positions.size() > 0) {
                                BigDecimal totalBuyNum = BigDecimal.ZERO;
                                BigDecimal totalSellNum = BigDecimal.ZERO;
                                BigDecimal totalBuyValue = BigDecimal.ZERO;
                                BigDecimal totalSellValue = BigDecimal.ZERO;

                                for (MemberContractPosition position : positions) {
                                    BigDecimal num = position.getPosition().multiply(position.getShareNumber());
                                    if (position.getDirection() == ContractOrderDirection.BUY) {
                                        totalBuyNum = totalBuyNum.add(num);
                                        totalBuyValue = totalBuyValue
                                                .add(position.getPrincipalAmount().multiply(position.getLeverage()));
                                    } else {
                                        totalSellNum = totalSellNum.add(num);
                                        totalSellValue = totalSellValue
                                                .add(position.getPrincipalAmount().multiply(position.getLeverage()));
                                    }

                                    if (totalBuyNum.compareTo(BigDecimal.ZERO) == 1
                                            && totalBuyValue.compareTo(BigDecimal.ZERO) == 1) {
                                        BigDecimal buyPrice = totalBuyValue.divide(totalBuyNum, 8,
                                                BigDecimal.ROUND_DOWN);
                                        if (buyPrice.compareTo(newPrice) == 1) {
                                            ks = (buyPrice.subtract(newPrice)).multiply(totalBuyNum);

                                        }
                                    }
                                    if (totalSellNum.compareTo(BigDecimal.ZERO) == 1
                                            && totalSellValue.compareTo(BigDecimal.ZERO) == 1) {
                                        BigDecimal sellPrice = totalSellValue.divide(totalSellNum, 8,
                                                BigDecimal.ROUND_DOWN);
                                        if (sellPrice.compareTo(newPrice) == -1) {
                                            ks = (newPrice.subtract(sellPrice)).multiply(totalSellNum);

                                        }
                                    }
                                }
                            }
                        }
                        Map<String, ContractCoin> coinMap = new HashMap<>();
                        for (String sy : symbols) {
                            ContractCoin coin = contractCoinService.findBySymbol(sy);
                            coinMap.put(sy, coin);
                            List<MemberContractPosition> positions = listMap.get(sy);
                            if (positions != null && positions.size() > 0) {
                                BigDecimal totalBuyNum = BigDecimal.ZERO;
                                BigDecimal totalSellNum = BigDecimal.ZERO;
                                BigDecimal totalBuyValue = BigDecimal.ZERO;
                                BigDecimal totalSellValue = BigDecimal.ZERO;
                                BigDecimal leverage = BigDecimal.ZERO;

                                for (MemberContractPosition position : positions) {
                                    BigDecimal num = position.getPosition().multiply(position.getShareNumber());
                                    leverage = position.getLeverage();
                                    if (position.getDirection() == ContractOrderDirection.BUY) {
                                        totalBuyNum = totalBuyNum.add(num);
                                        totalBuyValue = totalBuyValue
                                                .add(position.getPrincipalAmount().multiply(position.getLeverage()));
                                    } else {
                                        totalSellNum = totalSellNum.add(num);
                                        totalSellValue = totalSellValue
                                                .add(position.getPrincipalAmount().multiply(position.getLeverage()));
                                    }

                                }

                                ableBalance = ableBalance.subtract(ks);
                                BigDecimal mm = BigDecimal.ZERO;
                                BigDecimal tmm = BigDecimal.ZERO;

                                BigDecimal lastNum = BigDecimal.ZERO;
                                if (totalBuyNum.compareTo(totalSellNum) == 1) {

                                    lastNum = totalBuyNum.subtract(totalSellNum);

                                    BigDecimal buyPrice = totalBuyValue.divide(totalBuyNum, 8, BigDecimal.ROUND_DOWN);
                                    mm = lastNum.multiply(buyPrice).divide(leverage, 8, BigDecimal.ROUND_DOWN);
                                    tmm = lastNum.multiply(buyPrice).multiply(coin.getMaintenanceMarginRate());
                                    pdMap.put(sy, "BUY");

                                    prMap.put(sy, buyPrice);

                                } else if (totalBuyNum.compareTo(totalSellNum) == -1) {

                                    lastNum = totalSellNum.subtract(totalBuyNum);
                                    BigDecimal sellPrice = totalSellValue.divide(totalSellNum, 8,
                                            BigDecimal.ROUND_DOWN);

                                    mm = lastNum.multiply(sellPrice).divide(leverage, 8, BigDecimal.ROUND_DOWN);
                                    tmm = lastNum.multiply(sellPrice).multiply(coin.getMaintenanceMarginRate());
                                    pdMap.put(sy, "SELL");
                                    prMap.put(sy, sellPrice);

                                }

                                mmMap.put(sy, mm.subtract(tmm));
                                lastMap.put(sy, lastNum);
                            }

                        }
                        Boolean isBlast = false;
                        Map<String, BigDecimal> fpMap = new HashMap<>();

                        for (String sy : symbols) {
                            BigDecimal newPrice = mapPrice.get(sy);
                            BigDecimal m = mmMap.get(sy);
                            BigDecimal lastNum = lastMap.get(sy);

                            if ("BUY".equals(pdMap.get(sy))) {

                                BigDecimal forcePrice = prMap.get(sy)
                                        .subtract((ableBalance.add(m)).divide(lastNum, 8, BigDecimal.ROUND_DOWN));

                                if (forcePrice.compareTo(newPrice) >= 0) {
                                    isBlast = true;
                                }
                                fpMap.put(sy, forcePrice);
                            } else if ("SELL".equals(pdMap.get(sy))) {

                                BigDecimal forcePrice = prMap.get(sy)
                                        .add((ableBalance.add(m)).divide(lastNum, 8, BigDecimal.ROUND_DOWN));

                                if (forcePrice.compareTo(newPrice) <= 0) {
                                    isBlast = true;
                                }

                                fpMap.put(sy, forcePrice);
                            } else {

                                fpMap.put(sy, newPrice);
                            }

                        }

                        if (isBlast) {
                            BigDecimal totalPL = BigDecimal.ZERO;

                            for (MemberContractPosition position : list) {
                                totalPL = totalPL.add(position.getPrincipalAmount());
                                if (position.getDirection() == ContractOrderDirection.BUY) {
                                    blastBuy(wallet, position, mapPrice.get(position.getSymbol()),
                                            coinMap.get(position.getSymbol()));
                                } else {
                                    blastSell(wallet, position, mapPrice.get(position.getSymbol()),
                                            coinMap.get(position.getSymbol()));
                                }
                            }

                            List<ContractOrderEntrust> closingList = contractOrderEntrustService
                                    .queryAllEntrustClosingOrdersByContractCoin(wallet.getMemberId(),
                                            contractCoin.getId(), ContractOrderDirection.SELL);
                            for (ContractOrderEntrust item : closingList) {
                                cancelContractOrderEntrust(item, true);
                            }
                            closingList = contractOrderEntrustService.queryAllEntrustClosingOrdersByContractCoin(
                                    wallet.getMemberId(), contractCoin.getId(), ContractOrderDirection.BUY);
                            for (ContractOrderEntrust item : closingList) {
                                cancelContractOrderEntrust(item, true);
                            }

                            totalPL = totalPL.add(wallet.getUsdtBalance()).add(wallet.getUsdtFrozenBalance());
                            wallet.setUsdtBalance(BigDecimal.ZERO);
                            wallet.setUsdtFrozenBalance(BigDecimal.ZERO);
                            memberContractWalletService.updateById(wallet);
                            contractCoinService.increaseTotalProfit(contractCoin.getId(), totalPL);
                        }

                    }
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    @Transactional(rollbackFor = Exception.class)
    public void blastBuy(MemberContractWallet wallet, MemberContractPosition position, BigDecimal price,
            ContractCoin contractCoin) {

        BigDecimal num = position.getPosition();

        BigDecimal buyPL = BigDecimal.ZERO.subtract(position.getPrincipalAmount());

        BigDecimal closeFee = position.getPrincipalAmount().multiply(position.getLeverage())
                .multiply(contractCoin.getCloseFee());

        ContractOrderEntrust orderEntrust = new ContractOrderEntrust();
        orderEntrust.setContractId(contractCoin.getId());
        orderEntrust.setMemberId(wallet.getMemberId());
        orderEntrust.setSymbol(contractCoin.getSymbol());
        orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]);
        orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]);
        orderEntrust.setDirection(ContractOrderDirection.SELL);
        orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));

        orderEntrust.setTradedPrice(BigDecimal.ZERO);
        orderEntrust.setPrincipalUnit("USDT");
        orderEntrust.setPrincipalAmount(position.getPrincipalAmount());
        orderEntrust.setCreateTime(DateUtil.getTimeMillis());
        orderEntrust.setType(ContractOrderType.MARKET_PRICE);
        orderEntrust.setTriggerPrice(BigDecimal.ZERO);
        orderEntrust.setEntrustPrice(BigDecimal.ZERO);
        orderEntrust.setEntrustType(ContractOrderEntrustType.CLOSE);
        orderEntrust.setTriggeringTime(0L);
        orderEntrust.setShareNumber(position.getShareNumber());
        orderEntrust.setProfitAndLoss(buyPL);
        orderEntrust.setPatterns(ContractOrderPattern.creator(position.getPattern()));
        orderEntrust.setCloseFee(closeFee);
        orderEntrust.setCurrentPrice(price);
        orderEntrust.setIsBlast(1);
        orderEntrust.setPositionId(position.getId());
        orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS);
        boolean retObj = contractOrderEntrustService.save(orderEntrust);
        if (retObj) {

            handlePl(wallet.getMemberId(), buyPL);

            contractCoinService.increaseTotalCloseFee(contractCoin.getId(), closeFee);

            handleFee(wallet.getMemberId(), closeFee);

            position.setPrincipalAmount(BigDecimal.ZERO);
            position.setFrozenPrincipalAmount(BigDecimal.ZERO);
            position.setFrozenPosition(BigDecimal.ZERO);
            position.setPosition(BigDecimal.ZERO);
            position.setProfit(buyPL);
            memberContractPositionService.updateById(position);

            List<ContractOrderEntrust> closingList = contractOrderEntrustService.queryAllClosingOrdersByPositionId(
                    wallet.getMemberId(), position.getId(), contractCoin.getId(), ContractOrderDirection.SELL);
            for (ContractOrderEntrust item : closingList) {
                cancelContractOrderEntrust(item, true);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void blastSell(MemberContractWallet wallet, MemberContractPosition position, BigDecimal price,
            ContractCoin contractCoin) {

        BigDecimal sellPL = BigDecimal.ZERO.subtract(position.getPrincipalAmount());
        BigDecimal closeFee = position.getPrincipalAmount().multiply(position.getLeverage())
                .multiply(contractCoin.getCloseFee());

        ContractOrderEntrust orderEntrust = new ContractOrderEntrust();
        orderEntrust.setContractId(contractCoin.getId());
        orderEntrust.setMemberId(wallet.getMemberId());
        orderEntrust.setSymbol(contractCoin.getSymbol());
        orderEntrust.setBaseSymbol(contractCoin.getSymbol().split("/")[1]);
        orderEntrust.setCoinSymbol(contractCoin.getSymbol().split("/")[0]);
        orderEntrust.setDirection(ContractOrderDirection.BUY);
        orderEntrust.setContractOrderEntrustId(snowflakeConfig.getOrderId("CE"));

        orderEntrust.setTradedPrice(BigDecimal.ZERO);
        orderEntrust.setPrincipalUnit("USDT");
        orderEntrust.setPrincipalAmount(position.getPrincipalAmount());
        orderEntrust.setCreateTime(DateUtil.getTimeMillis());
        orderEntrust.setType(ContractOrderType.MARKET_PRICE);
        orderEntrust.setTriggerPrice(BigDecimal.ZERO);
        orderEntrust.setEntrustPrice(BigDecimal.ZERO);
        orderEntrust.setEntrustType(ContractOrderEntrustType.CLOSE);
        orderEntrust.setTriggeringTime(0L);
        orderEntrust.setShareNumber(position.getShareNumber());
        orderEntrust.setProfitAndLoss(sellPL);
        orderEntrust.setPatterns(ContractOrderPattern.creator(position.getPattern()));
        orderEntrust.setCloseFee(closeFee);
        orderEntrust.setCurrentPrice(price);
        orderEntrust.setIsBlast(1);
        orderEntrust.setStatus(ContractOrderEntrustStatus.ENTRUST_SUCCESS);
        boolean retObj = contractOrderEntrustService.save(orderEntrust);
        if (retObj) {

            handlePl(wallet.getMemberId(), sellPL);

            contractCoinService.increaseTotalCloseFee(contractCoin.getId(), closeFee);

            handleFee(wallet.getMemberId(), closeFee);

            position.setPrincipalAmount(BigDecimal.ZERO);
            position.setFrozenPrincipalAmount(BigDecimal.ZERO);
            position.setFrozenPosition(BigDecimal.ZERO);
            position.setPosition(BigDecimal.ZERO);
            position.setProfit(sellPL);
            memberContractPositionService.updateById(position);

            List<ContractOrderEntrust> closingList = contractOrderEntrustService.queryAllClosingOrdersByPositionId(
                    wallet.getMemberId(), position.getId(), contractCoin.getId(), ContractOrderDirection.BUY);
            for (ContractOrderEntrust item : closingList) {
                cancelContractOrderEntrust(item, true);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized void cancelContractOrderEntrust(ContractOrderEntrust orderEntrust, boolean isBlast) {

        LinkedList<ContractOrderEntrust> list = null;
        if (orderEntrust.getEntrustType() == ContractOrderEntrustType.OPEN) {
            if (orderEntrust.getType() == ContractOrderType.SPOT_LIMIT) {
                list = this.openOrderSpotList;

            } else {

                list = this.openOrderList;
            }
        } else {
            if (orderEntrust.getType() == ContractOrderType.SPOT_LIMIT) {
                list = this.closeOrderSpotList;

            } else {
                list = this.closeOrderList;

            }
        }
        synchronized (list) {

            Iterator<ContractOrderEntrust> orderIterator = list.iterator();
            while ((orderIterator.hasNext())) {
                ContractOrderEntrust order = orderIterator.next();

                if (order.getId().longValue() == orderEntrust.getId().longValue()) {

                    MemberContractWallet wallet = memberContractWalletService
                            .findByMemberId(orderEntrust.getMemberId());

                    if (orderEntrust.getEntrustType() == ContractOrderEntrustType.OPEN) {

                        if (!isBlast) {
                            memberContractWalletService.thawUsdtBalance(wallet,
                                    orderEntrust.getPrincipalAmount().add(orderEntrust.getOpenFee()));
                        }

                    } else {

                        if (!isBlast) {
                            if (orderEntrust.getPositionId() != null) {
                                MemberContractPosition position = memberContractPositionService
                                        .getById(orderEntrust.getPositionId());
                                if (position != null) {
                                    position.setFrozenPrincipalAmount(position.getFrozenPrincipalAmount()
                                            .subtract(orderEntrust.getPrincipalAmount()));
                                    position.setFrozenPosition(
                                            position.getFrozenPosition().subtract(orderEntrust.getVolume()));
                                    memberContractPositionService.saveOrUpdate(position);
                                }
                            }
                        }

                    }
                    contractOrderEntrustService.updateStatus(orderEntrust.getId(),
                            ContractOrderEntrustStatus.ENTRUST_CANCEL);
                    orderIterator.remove();
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
        this.thumb.setBaseUsdRate(BigDecimal.valueOf(6.68));
        this.thumb.setLastDayClose(BigDecimal.ZERO);
        this.thumb.setSymbol(this.symbol);
        this.thumb.setUsdRate(BigDecimal.valueOf(6.68));
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

    public String getSymbol() {
        return this.symbol;
    }

    public String getCoinSymbol() {
        return this.coinSymbol;
    }

    public String getBaseSymbol() {
        return this.baseSymbol;
    }

    public BigDecimal getNowPrice() {
        return this.nowPrice;
    }

    public CoinThumb getThumb() {
        return this.thumb;
    }

    public List<ContractTrade> getLastedTradeList() {
        return this.lastedTradeList;
    }

    public TradePlate getTradePlate(ContractOrderDirection direction) {
        if (direction == ContractOrderDirection.BUY) {
            return buyTradePlate;
        } else {
            return sellTradePlate;
        }
    }

    public void setContractCoinService(ContractCoinService contractCoinService) {
        this.contractCoinService = contractCoinService;
    }

    public void setMemberContractPositionService(MemberContractPositionService memberContractPositionService) {
        this.memberContractPositionService = memberContractPositionService;
    }

    public void setSnowflakeConfig(SnowflakeConfig snowflakeConfig) {
        this.snowflakeConfig = snowflakeConfig;
    }

    public void setContractOrderEntrustService(ContractOrderEntrustService contractOrderEntrustService) {
        this.contractOrderEntrustService = contractOrderEntrustService;
    }

    public void addHandler(MarketHandler storage) {
        handlers.add(storage);
    }

    public void setExchangePushJob(ExchangePushJob job) {
        this.exchangePushJob = job;
    }

    public void setMemberTransactionFeign(MemberTransactionFeign memberTransactionFeign) {
        this.memberTransactionFeign = memberTransactionFeign;
    }

    public void setMemberContractWalletService(MemberContractWalletService memberContractWalletService) {
        this.memberContractWalletService = memberContractWalletService;
    }

    public void setMemberTradeLimitService(MemberTradeLimitService memberTradeLimitService) {
        this.memberTradeLimitService = memberTradeLimitService;
    }

    
    public void updateContractCoin(ContractCoin coin) {
        synchronized (contractCoin) {
            contractCoin = coin;
        }
    }

    
    public void refreshBlastPrice(BigDecimal newPrice) {

        if (!this.isStarted)
            return;

    }

    
    public List<MemberContractWallet> getMemberContractWalletList() {
        return this.memberContractWalletList;
    }

    
    public void handleFee(Long memberId, BigDecimal fee) {

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(fee);
        memberTransaction.setMemberId(memberId);
        memberTransaction.setSymbol(contractCoin.getSymbol().split("/")[1]);
        memberTransaction.setType(TransactionType.CONTRACT_FEE.getCode());
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransactionFeign.save(memberTransaction);

    }

    
    public void handlePl(Long memberId, BigDecimal pL) {

        MemberTransaction memberTransaction = new MemberTransaction();
        memberTransaction.setFee(BigDecimal.ZERO);
        memberTransaction.setAmount(pL);
        memberTransaction.setMemberId(memberId);
        memberTransaction.setSymbol(contractCoin.getSymbol().split("/")[1]);
        memberTransaction.setType(pL.compareTo(BigDecimal.ZERO) > 0 ? TransactionType.CONTRACT_PROFIT.getCode()
                : TransactionType.CONTRACT_LOSS.getCode());
        memberTransaction.setCreateTime(DateUtil.getCurrentDate());
        memberTransaction.setRealFee("0");
        memberTransaction.setDiscountFee("0");
        memberTransactionFeign.save(memberTransaction);

    }

    
    public void memberWalletChange(Long walletId) {
        synchronized (memberContractWalletList) {
            boolean hasWallet = false;
            Iterator<MemberContractWallet> walletIterator = memberContractWalletList.iterator();
            while (walletIterator.hasNext()) {
                MemberContractWallet wallet = walletIterator.next();
                if (wallet.getId().longValue() == walletId.longValue()) {
                    hasWallet = true;

                    MemberContractWallet queryResult = memberContractWalletService.getById(walletId);
                    memberContractWalletList.set(memberContractWalletList.indexOf(wallet), queryResult);

                    break;
                }
            }

            if (!hasWallet) {
                MemberContractWallet wallet = memberContractWalletService.getById(walletId);
                if (wallet != null) {

                    memberContractWalletList.add(wallet);
                }
            }
        }
    }
}
