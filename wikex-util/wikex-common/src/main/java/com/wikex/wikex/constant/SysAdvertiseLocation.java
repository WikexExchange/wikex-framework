package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum SysAdvertiseLocation implements Serializable {
// App homepage carousel
APP_SHUFFLING(0, "App Homepage Carousel"),

// PC homepage carousel
PC_SHUFFLING(1, "PC Homepage Carousel"),

// PC classification advertisement
PC_CLASSIFICATION(2, "PC Classification Advertisement");


    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    SysAdvertiseLocation(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static SysAdvertiseLocation creator(Object v) {
        if(v instanceof String){
            for (SysAdvertiseLocation value : SysAdvertiseLocation.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (SysAdvertiseLocation value : SysAdvertiseLocation.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
