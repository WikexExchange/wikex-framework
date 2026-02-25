package com.wikex.wikex.match.trader;

import com.alibaba.fastjson.JSON;
import com.wikex.wikex.constant.ExchangeCoinPublishType;
import com.wikex.wikex.constant.ExchangeOrderDirection;
import com.wikex.wikex.constant.ExchangeOrderType;
import com.wikex.wikex.exchange.entity.ExchangeOrder;
import com.wikex.wikex.exchange.entity.MergeOrder;
import com.wikex.wikex.exchange.entity.TradePlate;
import com.wikex.wikex.exchange.util.OrderUtils;
import com.wikex.wikex.pojo.ExchangeTrade;
import com.wikex.wikex.util.Convert;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class CoinTrader implements Serializable {

    private static final long serialVersionUID = 1L;
    private String symbol;
    private RocketMQTemplate rocketMQTemplate;

    private int coinScale = 4;

    private int baseCoinScale = 4;
    private Logger logger = LoggerFactory.getLogger(CoinTrader.class);

    private TreeMap<BigDecimal, MergeOrder> buyLimitPriceQueue;

    private TreeMap<BigDecimal, MergeOrder> sellLimitPriceQueue;

    private LinkedList<ExchangeOrder> buyMarketQueue;

    private LinkedList<ExchangeOrder> sellMarketQueue;

    private TradePlate sellTradePlate;

    private TradePlate buyTradePlate;

    private boolean tradingHalt = false;
    private boolean ready = false;

    private ExchangeCoinPublishType publishType;
    private String clearTime;

    private SimpleDateFormat dateTimeFormat;

    public CoinTrader(String symbol) {
        this.symbol = symbol;
        initialize();
    }

    public void initialize() {
        this.buyLimitPriceQueue = new TreeMap<>(Comparator.reverseOrder());
        this.sellLimitPriceQueue = new TreeMap<>(Comparator.naturalOrder());
        this.buyMarketQueue = new LinkedList<>();
        this.sellMarketQueue = new LinkedList<>();
        this.sellTradePlate = new TradePlate(symbol, ExchangeOrderDirection.SELL);
        this.buyTradePlate = new TradePlate(symbol, ExchangeOrderDirection.BUY);
        this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * @param exchangeOrder
     */
    public void addLimitPriceOrder(ExchangeOrder exchangeOrder) {
        if (exchangeOrder.getType() != ExchangeOrderType.LIMIT_PRICE) {
            return;
        }

        TreeMap<BigDecimal, MergeOrder> list;
        if (exchangeOrder.getDirection() == ExchangeOrderDirection.BUY) {
            list = buyLimitPriceQueue;
            buyTradePlate.add(exchangeOrder);
            if (ready) {
                sendTradePlateMessage(buyTradePlate);
            }
        } else {
            list = sellLimitPriceQueue;
            sellTradePlate.add(exchangeOrder);
            if (ready) {
                sendTradePlateMessage(sellTradePlate);
            }
        }
        synchronized (list) {
            MergeOrder mergeOrder = list.get(exchangeOrder.getPrice());
            if (mergeOrder == null) {
                mergeOrder = new MergeOrder();
                mergeOrder.add(exchangeOrder);
                list.put(exchangeOrder.getPrice(), mergeOrder);
            } else {
                mergeOrder.add(exchangeOrder);
            }
        }
    }

    public void addMarketPriceOrder(ExchangeOrder exchangeOrder) {
        if (exchangeOrder.getType() != ExchangeOrderType.MARKET_PRICE) {
            return;
        }
        LinkedList<ExchangeOrder> list = exchangeOrder.getDirection() == ExchangeOrderDirection.BUY ? buyMarketQueue : sellMarketQueue;
        synchronized (list) {
            list.addLast(exchangeOrder);
        }
    }

    public void trade(List<ExchangeOrder> orders) throws ParseException {
        if (tradingHalt) {
            return;
        }
        for (ExchangeOrder order : orders) {
            trade(order);
        }
    }

    /**
     * @param exchangeOrder
     * @throws ParseException
     */
    public void trade(ExchangeOrder exchangeOrder) throws ParseException {
        if (tradingHalt) {
            return;
        }

        if (!symbol.equalsIgnoreCase(exchangeOrder.getSymbol())) {
            logger.info("unsupported symbol,coin={},base={}", exchangeOrder.getCoinSymbol(), exchangeOrder.getBaseSymbol());
            return;
        }
        if (exchangeOrder.getAmount().compareTo(BigDecimal.ZERO) <= 0 || exchangeOrder.getAmount().subtract(exchangeOrder.getTradedAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        TreeMap<BigDecimal, MergeOrder> limitPriceOrderList;
        LinkedList<ExchangeOrder> marketPriceOrderList;
        if (exchangeOrder.getDirection() == ExchangeOrderDirection.BUY) {
            limitPriceOrderList = sellLimitPriceQueue;
            marketPriceOrderList = sellMarketQueue;
        } else {
            limitPriceOrderList = buyLimitPriceQueue;
            marketPriceOrderList = buyMarketQueue;
        }
        if (exchangeOrder.getType() == ExchangeOrderType.MARKET_PRICE) {
            matchMarketPriceWithLPList(limitPriceOrderList, exchangeOrder);
        } else if (exchangeOrder.getType() == ExchangeOrderType.LIMIT_PRICE) {

            if (exchangeOrder.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }

            if (publishType == ExchangeCoinPublishType.FENTAN && exchangeOrder.getDirection() == ExchangeOrderDirection.SELL) {
                if (exchangeOrder.getTime().longValue() < dateTimeFormat.parse(clearTime).getTime()) {
                    matchLimitPriceWithLPListByFENTAN(limitPriceOrderList, exchangeOrder, false);
                    return;
                }
            }

            matchLimitPriceWithLPList(limitPriceOrderList, exchangeOrder, false);
            if (exchangeOrder.getAmount().compareTo(exchangeOrder.getTradedAmount()) > 0) {
                matchLimitPriceWithMPList(marketPriceOrderList, exchangeOrder);
            }
        }
    }

    /**
     * @param lpList
     * @param focusedOrder
     * @param canEnterList
     */

    public void matchLimitPriceWithLPListByFENTAN(TreeMap<BigDecimal, MergeOrder> lpList, ExchangeOrder focusedOrder, boolean canEnterList) {
        List<ExchangeTrade> exchangeTrades = new ArrayList<>();
        List<ExchangeOrder> completedOrders = new ArrayList<>();
        synchronized (lpList) {
            Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = lpList.entrySet().iterator();
            boolean exitLoop = false;
            while (!exitLoop && mergeOrderIterator.hasNext()) {
                Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
                MergeOrder mergeOrder = entry.getValue();
                Iterator<ExchangeOrder> orderIterator = mergeOrder.iterator();

                if (focusedOrder.getDirection() == ExchangeOrderDirection.BUY && mergeOrder.getPrice().compareTo(focusedOrder.getPrice()) > 0) {
                    break;
                }

                if (focusedOrder.getDirection() == ExchangeOrderDirection.SELL && mergeOrder.getPrice().compareTo(focusedOrder.getPrice()) < 0) {
                    break;
                }
                BigDecimal totalAmount = mergeOrder.getTotalAmount();
                while (orderIterator.hasNext()) {
                    ExchangeOrder matchOrder = orderIterator.next();

                    ExchangeTrade trade = processMatchByFENTAN(focusedOrder, matchOrder, totalAmount);
                    exchangeTrades.add(trade);

                    if (OrderUtils.isCompleted(matchOrder)) {

                        orderIterator.remove();
                        completedOrders.add(matchOrder);
                    }

                    if (OrderUtils.isCompleted(focusedOrder)) {

                        completedOrders.add(focusedOrder);

                        exitLoop = true;
                        break;
                    }
                }
                if (mergeOrder.size() == 0) {
                    mergeOrderIterator.remove();
                }
            }
        }

        if (focusedOrder.getTradedAmount().compareTo(focusedOrder.getAmount()) < 0 && canEnterList) {
            addLimitPriceOrder(focusedOrder);
        }

        handleExchangeTrade(exchangeTrades);
        if (completedOrders.size() > 0) {
            orderCompleted(completedOrders);
            TradePlate plate = focusedOrder.getDirection() == ExchangeOrderDirection.BUY ? sellTradePlate : buyTradePlate;
            sendTradePlateMessage(plate);
        }
    }

    /**
     * @param lpList
     * @param focusedOrder
     */
    public void matchLimitPriceWithLPList(TreeMap<BigDecimal, MergeOrder> lpList, ExchangeOrder focusedOrder, boolean canEnterList) {
        List<ExchangeTrade> exchangeTrades = new ArrayList<>();
        List<ExchangeOrder> completedOrders = new ArrayList<>();
        synchronized (lpList) {
            Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = lpList.entrySet().iterator();
            boolean exitLoop = false;
            while (!exitLoop && mergeOrderIterator.hasNext()) {
                Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
                MergeOrder mergeOrder = entry.getValue();
                Iterator<ExchangeOrder> orderIterator = mergeOrder.iterator();

                if (focusedOrder.getDirection() == ExchangeOrderDirection.BUY && mergeOrder.getPrice().compareTo(focusedOrder.getPrice()) > 0) {
                    break;
                }

                if (focusedOrder.getDirection() == ExchangeOrderDirection.SELL && mergeOrder.getPrice().compareTo(focusedOrder.getPrice()) < 0) {
                    break;
                }
                while (orderIterator.hasNext()) {
                    ExchangeOrder matchOrder = orderIterator.next();
                    ExchangeTrade trade = processMatch(focusedOrder, matchOrder);
                    exchangeTrades.add(trade);

                    if (OrderUtils.isCompleted(matchOrder)) {
                        orderIterator.remove();
                        completedOrders.add(matchOrder);
                    }

                    if (OrderUtils.isCompleted(focusedOrder)) {

                        completedOrders.add(focusedOrder);

                        exitLoop = true;
                        break;
                    }
                }
                if (mergeOrder.size() == 0) {
                    mergeOrderIterator.remove();
                }
            }
        }

        if (focusedOrder.getTradedAmount().compareTo(focusedOrder.getAmount()) < 0 && canEnterList) {
            addLimitPriceOrder(focusedOrder);
        }

        handleExchangeTrade(exchangeTrades);
        if (completedOrders.size() > 0) {
            orderCompleted(completedOrders);
            TradePlate plate = focusedOrder.getDirection() == ExchangeOrderDirection.BUY ? sellTradePlate : buyTradePlate;
            sendTradePlateMessage(plate);
        }
    }

    /**
     * @param mpList
     * @param focusedOrder
     */
    public void matchLimitPriceWithMPList(LinkedList<ExchangeOrder> mpList, ExchangeOrder focusedOrder) {
        List<ExchangeTrade> exchangeTrades = new ArrayList<>();
        List<ExchangeOrder> completedOrders = new ArrayList<>();
        synchronized (mpList) {
            Iterator<ExchangeOrder> iterator = mpList.iterator();
            while (iterator.hasNext()) {
                ExchangeOrder matchOrder = iterator.next();
                ExchangeTrade trade = processMatch(focusedOrder, matchOrder);
                if (trade != null) {
                    exchangeTrades.add(trade);
                }

                if (OrderUtils.isCompleted(matchOrder)) {
                    iterator.remove();
                    completedOrders.add(matchOrder);
                }

                if (OrderUtils.isCompleted(focusedOrder)) {

                    completedOrders.add(focusedOrder);

                    break;
                }
            }
        }

        if (focusedOrder.getTradedAmount().compareTo(focusedOrder.getAmount()) < 0) {
            addLimitPriceOrder(focusedOrder);
        }

        handleExchangeTrade(exchangeTrades);
        orderCompleted(completedOrders);
    }

    /**
     * @param lpList
     * @param focusedOrder
     */
    public void matchMarketPriceWithLPList(TreeMap<BigDecimal, MergeOrder> lpList, ExchangeOrder focusedOrder) {
        List<ExchangeTrade> exchangeTrades = new ArrayList<>();
        List<ExchangeOrder> completedOrders = new ArrayList<>();
        synchronized (lpList) {
            Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = lpList.entrySet().iterator();
            boolean exitLoop = false;
            while (!exitLoop && mergeOrderIterator.hasNext()) {
                Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
                MergeOrder mergeOrder = entry.getValue();
                Iterator<ExchangeOrder> orderIterator = mergeOrder.iterator();
                while (orderIterator.hasNext()) {
                    ExchangeOrder matchOrder = orderIterator.next();

                    ExchangeTrade trade = processMatch(focusedOrder, matchOrder);
                    if (trade != null) {
                        exchangeTrades.add(trade);
                    }

                    if (OrderUtils.isCompleted(matchOrder)) {

                        orderIterator.remove();
                        completedOrders.add(matchOrder);
                    }

                    if (OrderUtils.isCompleted(focusedOrder)) {
                        completedOrders.add(focusedOrder);

                        exitLoop = true;
                        break;
                    }
                }
                if (mergeOrder.size() == 0) {
                    mergeOrderIterator.remove();
                }
            }
        }

        if (focusedOrder.getDirection() == ExchangeOrderDirection.SELL
                && focusedOrder.getTradedAmount().compareTo(focusedOrder.getAmount()) < 0
                || focusedOrder.getDirection() == ExchangeOrderDirection.BUY
                && focusedOrder.getTurnover().compareTo(focusedOrder.getAmount()) < 0) {
            addMarketPriceOrder(focusedOrder);
        }

        handleExchangeTrade(exchangeTrades);
        if (completedOrders.size() > 0) {
            orderCompleted(completedOrders);
            TradePlate plate = focusedOrder.getDirection() == ExchangeOrderDirection.BUY ? sellTradePlate
                    : buyTradePlate;
            sendTradePlateMessage(plate);
        }
    }

    /**
     * @param order
     * @param dealPrice
     * @return
     */
    private BigDecimal calculateTradedAmount(ExchangeOrder order, BigDecimal dealPrice) {
        if (order.getDirection() == ExchangeOrderDirection.BUY && order.getType() == ExchangeOrderType.MARKET_PRICE) {

            BigDecimal leftTurnover = order.getAmount().subtract(order.getTurnover());
            return leftTurnover.divide(dealPrice, coinScale, BigDecimal.ROUND_DOWN);
        } else {
            return order.getAmount().subtract(order.getTradedAmount());
        }
    }

    /**
     * @param order
     * @param dealPrice
     * @return
     */
    private BigDecimal adjustMarketOrderTurnover(ExchangeOrder order, BigDecimal dealPrice) {
        if (order.getDirection() == ExchangeOrderDirection.BUY && order.getType() == ExchangeOrderType.MARKET_PRICE) {
            BigDecimal leftTurnover = order.getAmount().subtract(order.getTurnover());
            if (leftTurnover.divide(dealPrice, coinScale, BigDecimal.ROUND_DOWN).compareTo(BigDecimal.ZERO) == 0) {
                order.setTurnover(order.getAmount());
                return leftTurnover;
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * @param focusedOrder
     * @param matchOrder
     * @return
     */
    private ExchangeTrade processMatch(ExchangeOrder focusedOrder, ExchangeOrder matchOrder) {

        BigDecimal needAmount, dealPrice, availAmount;

        if (matchOrder.getType() == ExchangeOrderType.LIMIT_PRICE) {
            dealPrice = matchOrder.getPrice();
        } else {
            dealPrice = focusedOrder.getPrice();
        }

        if (dealPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        needAmount = calculateTradedAmount(focusedOrder, dealPrice);
        availAmount = calculateTradedAmount(matchOrder, dealPrice);

        BigDecimal tradedAmount = (availAmount.compareTo(needAmount) >= 0 ? needAmount : availAmount);

        if (tradedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        if (focusedOrder.getTurnover() == null) {
            focusedOrder.setTurnover(BigDecimal.ZERO);
        }
        if (matchOrder.getTurnover() == null) {
            matchOrder.setTurnover(BigDecimal.ZERO);
        }

        BigDecimal turnover = tradedAmount.multiply(dealPrice);
        matchOrder.setTradedAmount(matchOrder.getTradedAmount().add(tradedAmount));
        matchOrder.setTurnover(matchOrder.getTurnover().add(turnover));
        focusedOrder.setTradedAmount(focusedOrder.getTradedAmount().add(tradedAmount));
        focusedOrder.setTurnover(focusedOrder.getTurnover().add(turnover));

        ExchangeTrade exchangeTrade = new ExchangeTrade();
        exchangeTrade.setSymbol(symbol);
        exchangeTrade.setAmount(tradedAmount);
        exchangeTrade.setDirection(focusedOrder.getDirection());
        exchangeTrade.setPrice(dealPrice);
        exchangeTrade.setBuyTurnover(turnover);
        exchangeTrade.setSellTurnover(turnover);

        if (ExchangeOrderType.MARKET_PRICE == focusedOrder.getType() && focusedOrder.getDirection() == ExchangeOrderDirection.BUY) {
            BigDecimal adjustTurnover = adjustMarketOrderTurnover(focusedOrder, dealPrice);
            exchangeTrade.setBuyTurnover(turnover.add(adjustTurnover));
        } else if (ExchangeOrderType.MARKET_PRICE == matchOrder.getType() && matchOrder.getDirection() == ExchangeOrderDirection.BUY) {
            BigDecimal adjustTurnover = adjustMarketOrderTurnover(matchOrder, dealPrice);
            exchangeTrade.setBuyTurnover(turnover.add(adjustTurnover));
        }

        if (focusedOrder.getDirection() == ExchangeOrderDirection.BUY) {
            exchangeTrade.setBuyOrderId(focusedOrder.getOrderId());
            exchangeTrade.setSellOrderId(matchOrder.getOrderId());
        } else {
            exchangeTrade.setBuyOrderId(matchOrder.getOrderId());
            exchangeTrade.setSellOrderId(focusedOrder.getOrderId());
        }

        exchangeTrade.setTime(Calendar.getInstance().getTimeInMillis());
        if (matchOrder.getType() == ExchangeOrderType.LIMIT_PRICE) {
            if (matchOrder.getDirection() == ExchangeOrderDirection.BUY) {
                buyTradePlate.remove(matchOrder, tradedAmount);
            } else {
                sellTradePlate.remove(matchOrder, tradedAmount);
            }
        }
        return exchangeTrade;
    }

    /**
     * @param focusedOrder
     * @param matchOrder
     * @return
     */
    private ExchangeTrade processMatchByFENTAN(ExchangeOrder focusedOrder, ExchangeOrder matchOrder, BigDecimal totalAmount) {

        BigDecimal dealPrice;

        if (matchOrder.getType() == ExchangeOrderType.LIMIT_PRICE) {
            dealPrice = matchOrder.getPrice();
        } else {
            dealPrice = focusedOrder.getPrice();
        }

        if (dealPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal tradedAmount = focusedOrder.getAmount()
                .multiply(matchOrder.getAmount().divide(totalAmount, 8, BigDecimal.ROUND_HALF_DOWN))
                .setScale(8, BigDecimal.ROUND_HALF_DOWN);

        if (tradedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal turnover = tradedAmount.multiply(dealPrice).setScale(8, BigDecimal.ROUND_HALF_DOWN);
        matchOrder.setTradedAmount(matchOrder.getTradedAmount().add(tradedAmount).setScale(8, BigDecimal.ROUND_HALF_DOWN));
        matchOrder.setTurnover(matchOrder.getTurnover().add(turnover).setScale(8, BigDecimal.ROUND_HALF_DOWN));
        focusedOrder.setTradedAmount(focusedOrder.getTradedAmount().add(tradedAmount).setScale(8, BigDecimal.ROUND_HALF_DOWN));
        focusedOrder.setTurnover(focusedOrder.getTurnover().add(turnover).setScale(8, BigDecimal.ROUND_HALF_DOWN));

        ExchangeTrade exchangeTrade = new ExchangeTrade();
        exchangeTrade.setSymbol(symbol);
        exchangeTrade.setAmount(tradedAmount);
        exchangeTrade.setDirection(focusedOrder.getDirection());
        exchangeTrade.setPrice(dealPrice);
        exchangeTrade.setBuyTurnover(turnover);
        exchangeTrade.setSellTurnover(turnover);

        if (focusedOrder.getDirection() == ExchangeOrderDirection.BUY) {
            exchangeTrade.setBuyOrderId(focusedOrder.getOrderId());
            exchangeTrade.setSellOrderId(matchOrder.getOrderId());
        } else {
            exchangeTrade.setBuyOrderId(matchOrder.getOrderId());
            exchangeTrade.setSellOrderId(focusedOrder.getOrderId());
        }

        exchangeTrade.setTime(Calendar.getInstance().getTimeInMillis());
        if (matchOrder.getType() == ExchangeOrderType.LIMIT_PRICE) {
            if (matchOrder.getDirection() == ExchangeOrderDirection.BUY) {
                buyTradePlate.remove(matchOrder, tradedAmount);
            } else {
                sellTradePlate.remove(matchOrder, tradedAmount);
            }
        }
        return exchangeTrade;
    }

    public void handleExchangeTrade(List<ExchangeTrade> trades) {

        if (trades.size() > 0) {
            for (List<ExchangeTrade> chunks: Convert.chunkArrayList(trades, 500)) {
                rocketMQTemplate.convertAndSend("exchange-trade",JSON.toJSONString(chunks));
            }
        }
    }

    /**
     * @param orders
     */
    public void orderCompleted(List<ExchangeOrder> orders) {
        if (orders.size() > 0) {
            for (List<ExchangeOrder> chunks: Convert.chunkArrayList(orders, 500)) {
                rocketMQTemplate.convertAndSend("exchange-order-completed", JSON.toJSONString(chunks));
            }
        }
    }

    /**
     * @param plate
     */
    public void sendTradePlateMessage(TradePlate plate) {
        rocketMQTemplate.convertAndSend("exchange-trade-plate", plate.toJSONString());
    }

    /**
     * @param exchangeOrder
     * @return
     */
    public ExchangeOrder cancelOrder(ExchangeOrder exchangeOrder) {
        if (exchangeOrder.getType() == ExchangeOrderType.MARKET_PRICE) {

            Iterator<ExchangeOrder> orderIterator;
            List<ExchangeOrder> list = null;
            if (exchangeOrder.getDirection() == ExchangeOrderDirection.BUY) {
                list = this.buyMarketQueue;
            } else {
                list = this.sellMarketQueue;
            }
            synchronized (list) {
                orderIterator = list.iterator();
                while ((orderIterator.hasNext())) {
                    ExchangeOrder order = orderIterator.next();
                    if (order.getOrderId().equalsIgnoreCase(exchangeOrder.getOrderId())) {
                        orderIterator.remove();
                        onRemoveOrder(order);
                        return order;
                    }
                }
            }
        } else {
            TreeMap<BigDecimal, MergeOrder> list = null;
            if (exchangeOrder.getDirection() == ExchangeOrderDirection.BUY) {
                list = this.buyLimitPriceQueue;
            } else {
                list = this.sellLimitPriceQueue;
            }
            synchronized (list) {
                MergeOrder mergeOrder = list.get(exchangeOrder.getPrice());
                if (mergeOrder != null) {
                    Iterator<ExchangeOrder> orderIterator = mergeOrder.iterator();
                    while (orderIterator.hasNext()) {
                        ExchangeOrder order = orderIterator.next();
                        if (order.getOrderId().equalsIgnoreCase(exchangeOrder.getOrderId())) {
                            orderIterator.remove();
                            if (mergeOrder.size() == 0) {
                                list.remove(exchangeOrder.getPrice());
                            }
                            onRemoveOrder(order);
                            return order;
                        }
                    }
                }
            }
        }
        return null;
    }

    public void onRemoveOrder(ExchangeOrder order) {
        if (order.getType() == ExchangeOrderType.LIMIT_PRICE) {
            if (order.getDirection() == ExchangeOrderDirection.BUY) {
                buyTradePlate.remove(order);
                sendTradePlateMessage(buyTradePlate);
            } else {
                sellTradePlate.remove(order);
                sendTradePlateMessage(sellTradePlate);
            }
        }
    }

    public TradePlate getTradePlate(ExchangeOrderDirection direction) {
        if (direction == ExchangeOrderDirection.BUY) {
            return buyTradePlate;
        } else {
            return sellTradePlate;
        }
    }

    /**
     * @param orderId
     * @param type
     * @param direction
     * @return
     */
    public ExchangeOrder findOrder(String orderId, Integer type, Integer direction) {
        if (type == ExchangeOrderType.MARKET_PRICE.getCode()) {
            LinkedList<ExchangeOrder> list;
            if (direction == ExchangeOrderDirection.BUY.getCode()) {
                list = this.buyMarketQueue;
            } else {
                list = this.sellMarketQueue;
            }
            synchronized (list) {
                Iterator<ExchangeOrder> orderIterator = list.iterator();
                while ((orderIterator.hasNext())) {
                    ExchangeOrder order = orderIterator.next();
                    if (order.getOrderId().equalsIgnoreCase(orderId)) {
                        return order;
                    }
                }
            }
        } else {
            TreeMap<BigDecimal, MergeOrder> list;
            if (direction == ExchangeOrderDirection.BUY.getCode()) {
                list = this.buyLimitPriceQueue;
            } else {
                list = this.sellLimitPriceQueue;
            }
            synchronized (list) {
                Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = list.entrySet().iterator();
                while (mergeOrderIterator.hasNext()) {
                    Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
                    MergeOrder mergeOrder = entry.getValue();
                    Iterator<ExchangeOrder> orderIterator = mergeOrder.iterator();
                    while ((orderIterator.hasNext())) {
                        ExchangeOrder order = orderIterator.next();
                        if (order.getOrderId().equalsIgnoreCase(orderId)) {
                            return order;
                        }
                    }
                }
            }
        }
        return null;
    }

    public TreeMap<BigDecimal, MergeOrder> getBuyLimitPriceQueue() {
        return buyLimitPriceQueue;
    }

    public LinkedList<ExchangeOrder> getBuyMarketQueue() {
        return buyMarketQueue;
    }

    public TreeMap<BigDecimal, MergeOrder> getSellLimitPriceQueue() {
        return sellLimitPriceQueue;
    }

    public LinkedList<ExchangeOrder> getSellMarketQueue() {
        return sellMarketQueue;
    }

    public void setRocketMQTemplate(RocketMQTemplate template) {
        this.rocketMQTemplate = template;
    }

    public void setCoinScale(int scale) {
        this.coinScale = scale;
    }

    public void setBaseCoinScale(int scale) {
        this.baseCoinScale = scale;
    }

    public boolean isTradingHalt() {
        return this.tradingHalt;
    }

    public void haltTrading() {
        this.tradingHalt = true;
    }

    public void resumeTrading() {
        this.tradingHalt = false;
    }

    public void stopTrading() {
        // TODO:
    }

    public boolean getReady() {
        return this.ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public void setPublishType(ExchangeCoinPublishType publishType) {
        this.publishType = publishType;
    }

    public void setClearTime(String clearTime) {
        this.clearTime = clearTime;
    }

    /**
     * Set price threshold for BID and ASK TradePlates
     * @param bidPriceThreshold Price threshold for BID (e.g., 0.8 means 80% of highest price)
     * @param askPriceThreshold Price threshold for ASK (e.g., 1.2 means 120% of lowest price)
     */
    public void setPriceThreshold(BigDecimal bidPriceThreshold, BigDecimal askPriceThreshold) {
        if (bidPriceThreshold != null) {
            buyTradePlate.setPriceThreshold(bidPriceThreshold);
        } else {
            buyTradePlate.setPriceThreshold(BigDecimal.ZERO);
        }
        if (askPriceThreshold != null) {
            sellTradePlate.setPriceThreshold(askPriceThreshold);
        } else {
            sellTradePlate.setPriceThreshold(BigDecimal.ZERO);
        }
    }

    public int getLimitPriceOrderCount(ExchangeOrderDirection direction) {
        int count = 0;
        TreeMap<BigDecimal, MergeOrder> queue = direction == ExchangeOrderDirection.BUY ? buyLimitPriceQueue : sellLimitPriceQueue;
        Iterator<Map.Entry<BigDecimal, MergeOrder>> mergeOrderIterator = queue.entrySet().iterator();
        while (mergeOrderIterator.hasNext()) {
            Map.Entry<BigDecimal, MergeOrder> entry = mergeOrderIterator.next();
            MergeOrder mergeOrder = entry.getValue();
            count += mergeOrder.size();
        }
        return count;
    }
}