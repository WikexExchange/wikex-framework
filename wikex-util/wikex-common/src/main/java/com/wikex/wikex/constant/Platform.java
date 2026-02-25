package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum Platform implements Serializable {
    ANDROID(0, "Android"),

    IOS(1, "iOS");
    
    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    Platform(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static Platform creator(Object v) {
        if(v instanceof String){
            for (Platform value : Platform.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (Platform value : Platform.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
