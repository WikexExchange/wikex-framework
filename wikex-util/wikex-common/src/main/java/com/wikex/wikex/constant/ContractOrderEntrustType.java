package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum ContractOrderEntrustType {
    OPEN(0, "Open Position"),

    CLOSE(1, "Close Position");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractOrderEntrustType(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractOrderEntrustType creator(Object v) {
        if(v instanceof String){
            for (ContractOrderEntrustType value : ContractOrderEntrustType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractOrderEntrustType value : ContractOrderEntrustType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
