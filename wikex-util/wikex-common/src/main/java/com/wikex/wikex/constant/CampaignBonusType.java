package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum CampaignBonusType implements Serializable {
    TRADING_VOLUMN(0, "Trading Volumn"),
    FRIEND_TRANDE(1, "Inviter Trade"),
    INVITER(2, "Total Inviter"),
    COMMISION_FEE(3, "Inviter Trade Fee"),
    PNL_AMOUNT(4, "Pnl Amount");


    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    CampaignBonusType(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static CampaignBonusType creator(Object v) {
        if(v instanceof String){
            for (CampaignBonusType value : CampaignBonusType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (CampaignBonusType value : CampaignBonusType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
