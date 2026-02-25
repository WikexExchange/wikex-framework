package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum ExchangeOrderType implements Serializable {
    MARKET_PRICE(0, "Market Price"),

    LIMIT_PRICE(1, "Limit Price");
    

    @EnumValue
    private final int code;


    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ExchangeOrderType(int val, String description) {
        this.code = val;
        this.description = description;
    }


    @JsonCreator
    public static ExchangeOrderType creator(Object v) {
        if(v instanceof String){
            for (ExchangeOrderType value : ExchangeOrderType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ExchangeOrderType value : ExchangeOrderType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
