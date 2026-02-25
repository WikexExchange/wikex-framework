package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Objects;

public enum OrderTypeEnum implements Serializable {
    OTC(0,"OTC"),EXCHANGE(1,"EXCHANGE");

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    OrderTypeEnum(int val, String description) {
        this.code = val;
        this.description = description;
    }
    
    @JsonCreator
    public static OrderTypeEnum creator(Object v) {
        if(v instanceof String){
            for (OrderTypeEnum value : OrderTypeEnum.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (OrderTypeEnum value : OrderTypeEnum.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
