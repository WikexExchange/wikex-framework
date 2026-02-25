package com.wikex.wikex.pojo;


import com.alibaba.fastjson.JSONObject;
import com.wikex.wikex.constant.ContractOptionOrderDirection;
import com.wikex.wikex.constant.ContractOrderDirection;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Data
@Slf4j
public class OptionTradePlate {
    private List<TradePlateItem> items;
    
    private int maxDepth = 100;
    
    private ContractOptionOrderDirection direction;
    private String symbol;
    public OptionTradePlate(){

    }

    public OptionTradePlate(String symbol, ContractOptionOrderDirection direction) {
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
        if(direction == ContractOptionOrderDirection.BUY){
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
        if(direction == ContractOptionOrderDirection.BUY){
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
        json.put("direction",direction);
        json.put("maxAmount",getMaxAmount());
        json.put("minAmount",getMinAmount());
        json.put("highestPrice",getHighestPrice());
        json.put("lowestPrice",getLowestPrice());
        json.put("symbol",getSymbol());
        json.put("items",items);
        return json;
    }

    public JSONObject toJSON(int limit){
        JSONObject json = new JSONObject();
        json.put("direction",direction);
        json.put("maxAmount",getMaxAmount());
        json.put("minAmount",getMinAmount());
        json.put("highestPrice",getHighestPrice());
        json.put("lowestPrice",getLowestPrice());
        json.put("symbol",getSymbol());
        json.put("items",items.size() > limit ? new ArrayList<>(items.subList(0,limit)) : items);
        return json;
    }
}
