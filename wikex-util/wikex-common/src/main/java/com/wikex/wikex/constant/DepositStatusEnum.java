package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;



public enum DepositStatusEnum implements Serializable {
    PAY(0, "Pay"),

    GET_BACK(1, "Retrieve");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    DepositStatusEnum(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static DepositStatusEnum creator(Object v) {
        if(v instanceof String){
            for (DepositStatusEnum value : DepositStatusEnum.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (DepositStatusEnum value : DepositStatusEnum.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
