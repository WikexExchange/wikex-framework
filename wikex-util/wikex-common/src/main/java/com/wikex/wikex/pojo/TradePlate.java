package com.wikex.wikex.pojo;


import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOrderDirection;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Data
@Slf4j
public class TradePlate {
    private List<TradePlateItem> items;
    
    private int maxDepth = 100;
    
    private ContractOrderDirection direction;
    private String symbol;
    public TradePlate(){

    }

    public TradePlate(String symbol, ContractOrderDirection direction) {
        this.direction = direction;
        this.symbol = symbol;
        items = new ArrayList<>();
    }

    public void setItems(List<TradePlateItem> items){
        this.items = items;
    }

    public BigDecimal getHighestPrice(){
        if(items.size() == 0) {
            return BigDecimal.ZERO;
        }
        if(direction == ContractOrderDirection.BUY){
            return items.get(0).getPrice();
        }
        else{
            return items.get(items.size() - 1).getPrice();
        }
    }

    public int getDepth(){
        return items.size();
    }


    public BigDecimal getLowestPrice(){
        if(items.size() == 0) {
            return BigDecimal.ZERO;
        }
        if(direction == ContractOrderDirection.BUY){
            return items.get(items.size() - 1).getPrice();
        }
        else{
            return items.get(0).getPrice();
        }
    }

    
    public BigDecimal getMaxAmount(){
        if(items.size() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = BigDecimal.ZERO;
        for(TradePlateItem item:items){
            if(item.getAmount().compareTo(amount)>0){
                amount = item.getAmount();
            }
        }
        return amount;
    }

    
    public BigDecimal getMinAmount(){
        if(items.size() == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = items.get(0).getAmount();
        for(TradePlateItem item:items){
            if(item.getAmount().compareTo(amount) < 0){
                amount = item.getAmount();
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

                if (direction == ContractOrderDirection.BUY) {
                    lowestPrice = snapshot.get(snapshot.size()-1).getPrice();
                } else {
                    lowestPrice = snapshot.get(0).getPrice();
                }

                if (direction == ContractOrderDirection.BUY) {
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
//        JSONObject json = new JSONObject();
//        json.put("direction",direction);
//        json.put("maxAmount",getMaxAmount());
//        json.put("minAmount",getMinAmount());
//        json.put("highestPrice",getHighestPrice());
//        json.put("lowestPrice",getLowestPrice());
//        json.put("symbol",getSymbol());
//        json.put("items",items);
//        return json;
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

                if (direction == ContractOrderDirection.BUY) {
                    lowestPrice = snapshot.get(snapshot.size()-1).getPrice();
                } else {
                    lowestPrice = snapshot.get(0).getPrice();
                }

                if (direction == ContractOrderDirection.BUY) {
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

//        JSONObject json = new JSONObject();
//        json.put("direction",direction);
//        json.put("maxAmount",getMaxAmount());
//        json.put("minAmount",getMinAmount());
//        json.put("highestPrice",getHighestPrice());
//        json.put("lowestPrice",getLowestPrice());
//        json.put("symbol",getSymbol());
//        json.put("items",items.size() > limit ? new ArrayList<>(items.subList(0,limit)) : items);
//        return json;
    }
}
