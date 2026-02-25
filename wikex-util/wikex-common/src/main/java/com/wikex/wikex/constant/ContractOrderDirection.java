package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum ContractOrderDirection implements Serializable {
    BUY(0, "Long"),

    SELL(1, "Short");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractOrderDirection(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractOrderDirection creator(Object v) {
        if(v instanceof String){
            for (ContractOrderDirection value : ContractOrderDirection.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractOrderDirection value : ContractOrderDirection.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
