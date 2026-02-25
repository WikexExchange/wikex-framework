package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;


import java.io.Serializable;
import java.util.Objects;


public enum RealNameStatus implements Serializable {
    
    NOT_CERTIFIED(0, "Not Certified"),

    AUDITING(1, "Under Review"),
    
    VERIFIED(2, "Certified");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    RealNameStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static RealNameStatus creator(Object v) {
        if(v instanceof String){
            for (RealNameStatus value : RealNameStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (RealNameStatus value : RealNameStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
