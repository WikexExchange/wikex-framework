package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum LoginTypeEnum implements Serializable {

    EMAIL(0, "Email"),
    GOOGLE(1, "Google"),
    APPLE(2, "Apple ID");

    @EnumValue
    private final int code;

    private final String description;

    LoginTypeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    @JsonCreator
    public static LoginTypeEnum creator(Object v) {
        if (v instanceof String) {
            for (LoginTypeEnum value : LoginTypeEnum.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        } else {
            for (LoginTypeEnum value : LoginTypeEnum.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
