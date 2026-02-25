package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum AdvertiseLevel implements Serializable {

    ORDINARY(0, "Ordinary"),

    EXCELLENT(1, "Excellent");
    


    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    AdvertiseLevel(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static AdvertiseLevel creator(Object v) {
        if(v instanceof String){
            for (AdvertiseLevel value : AdvertiseLevel.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (AdvertiseLevel value : AdvertiseLevel.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
