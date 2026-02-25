package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum PromotionLevel implements Serializable {
    
    ONE(0, "Level 1"),

    TWO(1, "Level 2"),
    
    THREE(2, "Level 3");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    PromotionLevel(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static PromotionLevel creator(Object v) {
        if(v instanceof String){
            for (PromotionLevel value : PromotionLevel.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (PromotionLevel value : PromotionLevel.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
