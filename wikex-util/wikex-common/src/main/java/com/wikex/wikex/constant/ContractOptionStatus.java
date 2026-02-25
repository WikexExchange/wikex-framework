package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum ContractOptionStatus implements Serializable {
    STARTING(0, "Betting"),

    OPENING(1, "Opening"),
    
    CLOSED(2, "Announced"),
    
    CANCELED(3, "Canceled");
    


    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractOptionStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractOptionStatus creator(Object v) {
        if(v instanceof String){
            for (ContractOptionStatus value : ContractOptionStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractOptionStatus value : ContractOptionStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
