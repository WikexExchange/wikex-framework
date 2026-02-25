package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Objects;

public enum ContractOptionOrderStatus implements Serializable {
    OPEN(0, "Ongoing"),

    CLOSE(1, "Announced"),
    
    CANCELED(2, "Canceled");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractOptionOrderStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractOptionOrderStatus creator(Object v) {
        if(v instanceof String){
            for (ContractOptionOrderStatus value : ContractOptionOrderStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractOptionOrderStatus value : ContractOptionOrderStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
