package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum OptionOrderDirection implements Serializable {
    BUY(0, "Bullish"),

    SELL(1, "Bearish"),
    
    TIED(2, "Neutral");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    OptionOrderDirection(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static OptionOrderDirection creator(Object v) {
        if(v instanceof String){
            for (OptionOrderDirection value : OptionOrderDirection.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (OptionOrderDirection value : OptionOrderDirection.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
