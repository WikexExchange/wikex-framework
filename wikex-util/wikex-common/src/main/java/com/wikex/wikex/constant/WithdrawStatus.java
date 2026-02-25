package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;


import java.io.Serializable;
import java.util.Objects;


public enum WithdrawStatus implements Serializable {
    PROCESSING(0, "Under Review"),

    WAITING(1, "Waiting for Release"),
    
    FAIL(2, "Failed"),
    
    SUCCESS(3, "Successful");
        @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    WithdrawStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static WithdrawStatus creator(Object v) {
        if(v instanceof String){
            for (WithdrawStatus value : WithdrawStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (WithdrawStatus value : WithdrawStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
