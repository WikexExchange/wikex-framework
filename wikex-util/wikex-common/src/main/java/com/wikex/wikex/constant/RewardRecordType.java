package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum RewardRecordType implements Serializable {
    
    PROMOTION(0, "Promotion"), // register

    ACTIVITY(1, "Activity"),
    
    DIVIDEND(2, "Dividend"),

    CAMPAIGN_TRADING_VOLUMN_CLAIM(3, "Trade Bonus"),
    CAMPAIGN_TRADING_FRIEND_CLAIM(4, "Race With Friends"),
    CAMPAIGN_INVITER_CLAIM(5, "Friends & Network"),
    CAMPAIGN_EXCHANGE_FEE(6, "Exchange Fee");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    RewardRecordType(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static RewardRecordType creator(Object v) {
        if(v instanceof String){
            for (RewardRecordType value : RewardRecordType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (RewardRecordType value : RewardRecordType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }

}
