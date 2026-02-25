package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum BooleanEnum implements Serializable {
    IS_FALSE(0, "No"),

    IS_TRUE(1, "Yes");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public boolean getValue() {
        if(this.code==0){
            return false;
        }else {
            return true;
        }
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    BooleanEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonCreator
    public static BooleanEnum creator(Object v) {
        if(v instanceof String){
            for (BooleanEnum value : BooleanEnum.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (BooleanEnum value : BooleanEnum.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
