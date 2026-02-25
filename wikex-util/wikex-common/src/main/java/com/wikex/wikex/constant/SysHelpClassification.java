package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum SysHelpClassification implements Serializable {
    HELP(0, "Getting Started"),

    FAQ(1, "FAQ"),
    
    EXCHANGE(2, "Trading Guide"),
    
    COININFO(3, "Coin Information"),
    
    ANALYSIS(4, "Market Analysis"),
    
    PROTOCOL(5, "Terms and Agreements"),
    
    OTHER(6, "Other"),
    
    QR_CODE(7, "App QR Code");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    SysHelpClassification(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static SysHelpClassification creator(Object v) {
        if(v instanceof String){
            for (SysHelpClassification value : SysHelpClassification.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (SysHelpClassification value : SysHelpClassification.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
