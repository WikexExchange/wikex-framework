package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum ContractOrderPattern implements Serializable {
    CROSSED(0, "Cross Margin"),

    FIXED(1, "Isolated Margin");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractOrderPattern(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractOrderPattern creator(Object v) {
        if(v instanceof String){
            for (ContractOrderPattern value : ContractOrderPattern.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractOrderPattern value : ContractOrderPattern.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
