package com.wikex.wikex.exchange.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ExchangeOrderDirection;
import com.wikex.wikex.constant.ExchangeOrderType;
import com.wikex.wikex.pojo.TradePlateItem;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Order Book (Trade Plate) Data
 */
@ApiModel(value = "Order Book Data")
@Data
@Slf4j
public class TradePlate {
    private List<TradePlateItem> items;
    private int maxDepth = 100;
    private ExchangeOrderDirection direction;
    private String symbol;
    private BigDecimal priceThreshold = BigDecimal.ZERO; // 0.8 for BID, 1.2 for ASK

    public TradePlate(){

    }

    public TradePlate(String symbol, ExchangeOrderDirection direction) {
        this.direction = direction;
        this.symbol = symbol;
        items = new LinkedList<TradePlateItem>();
    }

    public void setPriceThreshold(BigDecimal priceThreshold) {
        this.priceThreshold = priceThreshold;
    }

    public boolean add(ExchangeOrder exchangeOrder) {
        synchronized (items) {
            int index = 0;
            if (exchangeOrder.getType() == ExchangeOrderType.MARKET_PRICE) {
                return false;
            }
            if (exchangeOrder.getDirection() != direction) {
                return false;
            }

            BigDecimal orderPrice = exchangeOrder.getPrice();
            // Check price threshold before adding (to prevent list from growing too large)
            if (items.size() > 0 && priceThreshold.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal referencePrice;
                if (direction == ExchangeOrderDirection.BUY) {
                    // BID: Check if price >= highestPrice * threshold (e.g., 0.8 = 80%)
                    referencePrice = items.get(0).getPrice(); // Highest price (first item for BID)
                    BigDecimal minPrice = referencePrice.multiply(priceThreshold);
                    if (orderPrice.compareTo(minPrice) < 0) {
                        // Price is too low, don't add to TradePlate
                        return false;
                    }
                } else {
                    // ASK: Check if price <= lowestPrice * threshold (e.g., 1.2 = 120%)
                    referencePrice = items.get(0).getPrice(); // Lowest price (first item for ASK)
                    BigDecimal maxPrice = referencePrice.multiply(priceThreshold);
                    if (orderPrice.compareTo(maxPrice) > 0) {
                        // Price is too high, don't add to TradePlate
                        return false;
                    }
                }
            }

            if (items.size() > 0) {
                for (index = 0; index < items.size(); index++) {
                    TradePlateItem item = items.get(index);
                    if (exchangeOrder.getDirection() == ExchangeOrderDirection.BUY && item.getPrice().compareTo(exchangeOrder.getPrice()) > 0
                            || exchangeOrder.getDirection() == ExchangeOrderDirection.SELL && item.getPrice().compareTo(exchangeOrder.getPrice()) < 0) {
                        continue;
                    } else if (item.getPrice().compareTo(exchangeOrder.getPrice()) == 0) {
                        BigDecimal deltaAmount = exchangeOrder.getAmount().subtract(exchangeOrder.getTradedAmount());
                        item.setAmount(item.getAmount().add(deltaAmount));
                        return true;
                    } else {
                        break;
                    }
                }
            }

            // Cleanup items outside threshold periodically (only when list is getting large)
            // This reduces CPU usage while still keeping the list manageable
            if (priceThreshold.compareTo(BigDecimal.ZERO) > 0) {
                cleanupItemsOutsideThreshold();
            }

            if(index < maxDepth) {
                TradePlateItem newItem = new TradePlateItem();
                newItem.setAmount(exchangeOrder.getAmount().subtract(exchangeOrder.getTradedAmount()));
                newItem.setPrice(exchangeOrder.getPrice());
                items.add(index, newItem);
            }
        }
        return true;
    }

    /**
     * Remove items that are outside the price threshold
     * This helps keep the TradePlate list small
     */
    private void cleanupItemsOutsideThreshold() {
        if (items == null || items.size() == 0 || priceThreshold.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal referencePrice;
        if (direction == ExchangeOrderDirection.BUY) {
            // BID: Keep only items with price >= highestPrice * threshold
            referencePrice = items.get(0).getPrice();
            BigDecimal minPrice = referencePrice.multiply(priceThreshold);

            // Remove items from the end (lowest prices) that are below threshold
            for (int i = items.size() - 1; i >= 0; i--) {
                TradePlateItem item = items.get(i);
                if (item.getPrice().compareTo(minPrice) < 0) {
                    items.remove(i);
                } else {
                    // Items are sorted descending, so we can stop here
                    break;
                }
            }
        } else {
            // ASK: Keep only items with price <= lowestPrice * threshold
            referencePrice = items.get(0).getPrice();
            BigDecimal maxPrice = referencePrice.multiply(priceThreshold);

            // Remove items from the end (highest prices) that are above threshold
            for (int i = items.size() - 1; i >= 0; i--) {
                TradePlateItem item = items.get(i);
                if (item.getPrice().compareTo(maxPrice) > 0) {
                    items.remove(i);
                } else {
                    // Items are sorted ascending, so we can stop here
                    break;
                }
            }
        }
    }

    public void remove(ExchangeOrder order,BigDecimal amount) {
        synchronized (items) {
            for (int index = 0; index < items.size(); index++) {
                try {
                    TradePlateItem item = items.get(index);
                    if (item.getPrice().compareTo(order.getPrice()) == 0) {
                        item.setAmount(item.getAmount().subtract(amount));
                        if (item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                            items.remove(index);
                        }
                        return;
                    }
                } catch (Exception e) {
                    // TODO
                }
            }
        }
    }

    public void remove(ExchangeOrder order){
        remove(order,order.getAmount().subtract(order.getTradedAmount()));
    }

    public void setItems(LinkedList<TradePlateItem> items){
        this.items = items;
    }

    public BigDecimal getHighestPrice(){
        List<TradePlateItem> snapshot;
        synchronized (items) {
            snapshot = new ArrayList<>(items);
        }

        if (snapshot.size() == 0) {
            return BigDecimal.ZERO;
        }
        if (direction == ExchangeOrderDirection.BUY) {
            return snapshot.get(0).getPrice();
        } else {
            return snapshot.get(snapshot.size()-1).getPrice();
        }
    }

    public int getDepth(){
        return items==null?0:items.size();
    }


    public BigDecimal getLowestPrice(){
        List<TradePlateItem> snapshot;
        synchronized (items) {
            snapshot = new ArrayList<>(items);
        }

        if (snapshot.size() == 0) {
            return BigDecimal.ZERO;
        }
        if (direction == ExchangeOrderDirection.BUY) {
            return snapshot.get(snapshot.size()-1).getPrice();
        } else {
            return snapshot.get(0).getPrice();
        }
    }

    /**
     * Get the largest order size in the current order book.
     */
    public BigDecimal getMaxAmount(){
        List<TradePlateItem> snapshot;
        synchronized (items) {
            snapshot = new ArrayList<>(items);
        }

        if (snapshot.size() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = BigDecimal.ZERO;
        for (TradePlateItem item : snapshot) {
            try {
                if (item.getAmount().compareTo(amount) > 0) {
                    amount = item.getAmount();
                }
            } catch (Exception e) {
                // TODO
            }
        }
        return amount;
    }

    /**
     * Get the smallest order size in the current order book.
     */
    public BigDecimal getMinAmount(){
        List<TradePlateItem> snapshot;
        synchronized (items) {
            snapshot = new ArrayList<>(items);
        }

        if (items == null || snapshot.size() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = snapshot.get(0).getAmount();
        for (TradePlateItem item : snapshot) {
            try {
                if (item.getAmount().compareTo(amount) < 0) {
                    amount = item.getAmount();
                }
            } catch (Exception e) {
                // TODO
            }
        }
        return amount;
    }

    public JSONObject toJSON(){
        JSONObject json = new JSONObject();
        try {
            List<TradePlateItem> snapshot;
            synchronized (items) {
                snapshot = new ArrayList<>(items);
            }

            BigDecimal minAmount = BigDecimal.ZERO;
            BigDecimal maxAmount = BigDecimal.ZERO;
            BigDecimal highPrice = BigDecimal.ZERO;
            BigDecimal lowestPrice = BigDecimal.ZERO;
            if (snapshot.size() > 0) {
                minAmount = snapshot.get(0).getAmount();
                for (TradePlateItem item : snapshot) {
                    if (item.getAmount().compareTo(minAmount) < 0) {
                        minAmount = item.getAmount();
                    }
                    if (item.getAmount().compareTo(maxAmount) > 0) {
                        maxAmount = item.getAmount();
                    }
                }

                if (direction == ExchangeOrderDirection.BUY) {
                    lowestPrice = snapshot.get(snapshot.size()-1).getPrice();
                } else {
                    lowestPrice = snapshot.get(0).getPrice();
                }

                if (direction == ExchangeOrderDirection.BUY) {
                    highPrice = snapshot.get(0).getPrice();
                } else {
                    highPrice = snapshot.get(snapshot.size()-1).getPrice();
                }
            }

            json.put("direction", direction);
            json.put("maxAmount", maxAmount);
            json.put("minAmount", minAmount);
            json.put("highestPrice", highPrice);
            json.put("lowestPrice", lowestPrice);
            json.put("symbol", getSymbol());
            json.put("items", snapshot);

            return json;
        } catch (Exception ex) {
            return json;
        }
    }

    public JSONObject toJSON(int limit){
        JSONObject json = new JSONObject();
        try {
            List<TradePlateItem> snapshot;
            synchronized (items) {
                snapshot = new ArrayList<>(items);
            }

            BigDecimal minAmount = BigDecimal.ZERO;
            BigDecimal maxAmount = BigDecimal.ZERO;
            BigDecimal highPrice = BigDecimal.ZERO;
            BigDecimal lowestPrice = BigDecimal.ZERO;
            if (snapshot.size() > 0) {
                minAmount = snapshot.get(0).getAmount();
                for (TradePlateItem item : snapshot) {
                    if (item.getAmount().compareTo(minAmount) < 0) {
                        minAmount = item.getAmount();
                    }
                    if (item.getAmount().compareTo(maxAmount) > 0) {
                        maxAmount = item.getAmount();
                    }
                }

                if (direction == ExchangeOrderDirection.BUY) {
                    lowestPrice = snapshot.get(snapshot.size()-1).getPrice();
                } else {
                    lowestPrice = snapshot.get(0).getPrice();
                }

                if (direction == ExchangeOrderDirection.BUY) {
                    highPrice = snapshot.get(0).getPrice();
                } else {
                    highPrice = snapshot.get(snapshot.size()-1).getPrice();
                }
            }

            json.put("direction", direction);
            json.put("maxAmount", maxAmount);
            json.put("minAmount", minAmount);
            json.put("highestPrice", highPrice);
            json.put("lowestPrice", lowestPrice);
            json.put("symbol", getSymbol());

            int size = Math.min(limit, snapshot.size());
            json.put("items", snapshot.subList(0, size));

            return json;
        } catch (Exception e){
            return json;
        }
    }


    public String toJSONString(){
        synchronized (items){
            return JSON.toJSONString(this);
        }
    }
}