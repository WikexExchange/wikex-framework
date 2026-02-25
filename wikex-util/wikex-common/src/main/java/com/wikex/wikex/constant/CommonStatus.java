package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Objects;


public enum CommonStatus implements Serializable {
    
    NORMAL(0, "Normal"),

    ILLEGAL(1, "Illegal");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    CommonStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static CommonStatus creator(Object v) {
        if(v instanceof String){
            for (CommonStatus value : CommonStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (CommonStatus value : CommonStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }

}
