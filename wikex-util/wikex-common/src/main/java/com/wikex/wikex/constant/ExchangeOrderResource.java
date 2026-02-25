package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Objects;


public enum ExchangeOrderResource implements Serializable {

    ROBOT(0, "Robot"),

    CUSTOMER(1, "User"), //1
    
    API(2, "API");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ExchangeOrderResource(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ExchangeOrderResource creator(Object v) {
        if(v instanceof String){
            for (ExchangeOrderResource value : ExchangeOrderResource.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ExchangeOrderResource value : ExchangeOrderResource.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
