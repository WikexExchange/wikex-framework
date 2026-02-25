package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum ExchangeOrderStatus implements Serializable {
    TRADING(0, "Trading"),

    COMPLETED(1, "Completed"), //1
    
    CANCELED(2, "Canceled"), //2
    
    OVERTIMED(3, "Timed Out"); //3
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ExchangeOrderStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ExchangeOrderStatus creator(Object v) {
        if(v instanceof String){
            for (ExchangeOrderStatus value : ExchangeOrderStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ExchangeOrderStatus value : ExchangeOrderStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
