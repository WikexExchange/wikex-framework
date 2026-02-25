package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum CampaignPointType implements Serializable {
    REGISTER(1, "Register"),
    FRIEND_REFERRAL_RANKING_WEEK(2, "Weekly Friend Referral Ranking"),
    COMMISION_RANKING_WEEK(3, "Weekly Commission Ranking"),
    TRADING_VOLUMN_RANKING_WEEK(4, "Weekly Trading Volume Ranking"),
    PNL_AMOUNT_RANKING_WEEK(5, "Weekly PnL Amount Ranking"),
    FRIEND_REFERRAL_RANKING_MONTH(6, "Monthly Friend Referral Ranking"),
    COMMISION_RANKING_MONTH(7, "Monthly Commission Ranking"),
    TRADING_VOLUMN_RANKING_MONTH(8, "Monthly Trading Volume Ranking"),
    PNL_AMOUNT_RANKING_MONTH(9, "Monthly PnL Amount Ranking"),
    REFERRER(10, "Referrer");

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    CampaignPointType(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static CampaignPointType creator(Object v) {
        if(v instanceof String){
            for (CampaignPointType value : CampaignPointType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (CampaignPointType value : CampaignPointType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
