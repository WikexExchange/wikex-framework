package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public enum ContractSecondOrderResult {
    WAIT(0, "Pending"),

    WIN(1, "Win"),
    
    LOSE(2, "Lose"),
    
    TIED(3, "Draw"),
    
    CANCELED(4, "Canceled");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractSecondOrderResult(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractSecondOrderResult creator(Object v) {
        if(v instanceof String){
            for (ContractSecondOrderResult value : ContractSecondOrderResult.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractSecondOrderResult value : ContractSecondOrderResult.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
