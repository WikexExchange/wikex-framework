package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum ContractOrderType implements Serializable {
    MARKET_PRICE(0, "Market Price"),

    LIMIT_PRICE(1, "Limit Price"),
    
    SPOT_LIMIT(2, "Planned Entrust");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractOrderType(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractOrderType creator(Object v) {
        if(v instanceof String){
            for (ContractOrderType value : ContractOrderType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractOrderType value : ContractOrderType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
