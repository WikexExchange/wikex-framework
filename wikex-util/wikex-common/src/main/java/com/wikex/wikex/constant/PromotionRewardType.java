package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum PromotionRewardType implements Serializable {
    
    REGISTER(0, "Referral Registration"),

    TRANSACTION(1, "Fiat Referral Transaction"),
    
    EXCHANGE_TRANSACTION(2, "Spot Referral Transaction");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    PromotionRewardType(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static PromotionRewardType creator(Object v) {
        if(v instanceof String){
            for (PromotionRewardType value : PromotionRewardType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (PromotionRewardType value : PromotionRewardType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
