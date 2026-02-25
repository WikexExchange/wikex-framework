package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum LegalWalletState implements Serializable {
    APPLYING(0, "Applying"),

    COMPLETE(1, "Completed"),
    
    DEFEATED(2, "Failed");
    
    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    LegalWalletState(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static LegalWalletState creator(Object v) {
        if(v instanceof String){
            for (LegalWalletState value : LegalWalletState.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (LegalWalletState value : LegalWalletState.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
