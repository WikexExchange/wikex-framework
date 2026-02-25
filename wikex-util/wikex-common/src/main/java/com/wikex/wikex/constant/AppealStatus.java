package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum AppealStatus implements Serializable {
    NOT_PROCESSED(0, "Not Processed"),

    PROCESSED(1, "Processed");

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public boolean getValue() {
        if (this.code == 0) {
            return false;
        } else {
            return true;
        }
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    AppealStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @JsonCreator
    public static AppealStatus creator(Object v) {
        if (v instanceof String) {
            for (AppealStatus value : AppealStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        } else {
            for (AppealStatus value : AppealStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }

}
