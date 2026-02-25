package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum ContractSecondOrderDirection {
    BUY(0, "Bullish"),

    SELL(1, "Bearish");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractSecondOrderDirection(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractSecondOrderDirection creator(Object v) {
        if(v instanceof String){
            for (ContractSecondOrderDirection value : ContractSecondOrderDirection.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractSecondOrderDirection value : ContractSecondOrderDirection.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
