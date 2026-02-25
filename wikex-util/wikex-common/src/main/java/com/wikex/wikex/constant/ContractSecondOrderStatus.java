package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public enum ContractSecondOrderStatus {
    ENTRUST(0, "Entrusting"),

    OPEN(1, "Holding Position"),
    
    CLOSE(2, "Completed"),
    
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

    ContractSecondOrderStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractSecondOrderStatus creator(Object v) {
        if(v instanceof String){
            for (ContractSecondOrderStatus value : ContractSecondOrderStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractSecondOrderStatus value : ContractSecondOrderStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
