package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public enum WalletType {
    SPOT(0, "Spot Wallet"),

    SWAP(1, "Contract Wallet"),

    SECOND(2, "Second Contract Wallet"),

    FUNDING(3, "Funding Wallet");

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    WalletType(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static WalletType creator(Object v) {
        if (v instanceof String) {
            for (WalletType value : WalletType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        } else {
            for (WalletType value : WalletType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
