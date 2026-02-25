package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum ContractOptionResult {
    WAIT(0, "Pending"),

    WIN(1, "Rise"),
    
    LOSE(2, "Fall"),
    
    TIED(3, "Flat"),
    
    CANCELED(3, "Canceled");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractOptionResult(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractOptionResult creator(Object v) {
        if(v instanceof String){
            for (ContractOptionResult value : ContractOptionResult.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractOptionResult value : ContractOptionResult.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
