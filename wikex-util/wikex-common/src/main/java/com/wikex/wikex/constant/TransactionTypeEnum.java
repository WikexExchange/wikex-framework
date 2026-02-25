package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;


import java.io.Serializable;
import java.util.Objects;

public enum TransactionTypeEnum implements Serializable {
    OTC_NUM(0, "Fiat Trading Volume"),

    OTC_MONEY(1, "Fiat Trading Amount"),
    
    EXCHANGE(2, "Spot Trading Fee Statistics"),
    
    EXCHANGE_COIN(3, "Spot Trading Volume Statistics"),
    
    EXCHANGE_BASE(4, "Spot Trading Amount Statistics"),
    
    RECHARGE(5, "Deposit"),
    
    WITHDRAW(6, "Withdrawal");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    TransactionTypeEnum(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static TransactionTypeEnum creator(Object v) {
        if(v instanceof String){
            for (TransactionTypeEnum value : TransactionTypeEnum.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (TransactionTypeEnum value : TransactionTypeEnum.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
