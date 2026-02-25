package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum MemberLevelEnum implements Serializable {

    GENERAL(0, "General"),

    REALNAME(1, "Real-name"),
    
    IDENTIFICATION(2, "Certified Merchant");
    


    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    MemberLevelEnum(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static MemberLevelEnum creator(Object v) {
        if(v instanceof String){
            for (MemberLevelEnum value : MemberLevelEnum.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (MemberLevelEnum value : MemberLevelEnum.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }

}
