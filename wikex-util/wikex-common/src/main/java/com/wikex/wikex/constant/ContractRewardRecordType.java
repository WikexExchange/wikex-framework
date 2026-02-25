package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Objects;

public enum ContractRewardRecordType implements Serializable {
    
    OPEN(0, "Open Position Rebate"),

    CLOSE(1, "Close Position Rebate"),
    
    LEVEL(2, "Peer-level Reward"),
    
    PLATFORM(3, "Platform Fee Income");
    
    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractRewardRecordType(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractRewardRecordType creator(Object v) {
        if(v instanceof String){
            for (ContractRewardRecordType value : ContractRewardRecordType.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractRewardRecordType value : ContractRewardRecordType.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }

}
