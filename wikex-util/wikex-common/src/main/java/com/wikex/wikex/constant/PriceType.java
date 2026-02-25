package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum PriceType implements Serializable {
    
    REGULAR(0, "Fixed"),

    MUTATIVE(1, "Variable");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public boolean getValue() {
        if(this.code==0){
            return false;
        }else {
            return true;
        }
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    PriceType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonCreator
    public static PriceType creator(Object v) {
        if(v instanceof String){
            for (PriceType value : PriceType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (PriceType value : PriceType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
