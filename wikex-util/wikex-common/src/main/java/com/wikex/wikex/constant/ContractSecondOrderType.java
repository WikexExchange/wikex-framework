package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public enum ContractSecondOrderType {
    NO(0, "No Compensation"),

    YES(1, "With Compensation");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractSecondOrderType(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractSecondOrderType creator(Object v) {
        if(v instanceof String){
            for (ContractSecondOrderType value : ContractSecondOrderType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractSecondOrderType value : ContractSecondOrderType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }

}
