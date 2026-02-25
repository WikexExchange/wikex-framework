package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum AdvertiseControlStatus implements Serializable {

    PUT_ON_SHELVES(0, "Put on shelves"),

    PUT_OFF_SHELVES(1, "Take off shelves"),

    TURNOFF(2, "Closed");

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    AdvertiseControlStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static AdvertiseControlStatus creator(Object v) {
        if (v instanceof String) {
            for (AdvertiseControlStatus value : AdvertiseControlStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        } else {
            for (AdvertiseControlStatus value : AdvertiseControlStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
