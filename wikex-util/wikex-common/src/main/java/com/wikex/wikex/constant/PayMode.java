package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum PayMode implements Serializable {
    ALI(0, "Alipay"),

    WECHAT(1, "WeChat"),
    
    BANK(2, "UnionPay");
    
    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    PayMode(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static PayMode creator(Object v) {
        if(v instanceof String){
            for (PayMode value : PayMode.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (PayMode value : PayMode.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
