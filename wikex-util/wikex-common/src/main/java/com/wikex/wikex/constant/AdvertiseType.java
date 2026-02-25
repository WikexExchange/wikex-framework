package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum AdvertiseType implements Serializable {

    
    BUY(0, "Buy"),

    SELL(1, "Sell");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    AdvertiseType(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static AdvertiseType creator(Object v) {
        if(v instanceof String){
            for (AdvertiseType value : AdvertiseType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (AdvertiseType value : AdvertiseType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }

}
