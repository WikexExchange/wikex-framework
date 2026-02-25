package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;


public enum AdminModule {
    CMS(0,"CMS"),
    COMMON(1,"COMMON"),
    EXCHANGE(2,"EXCHANGE"),
    FINANCE(3,"FINANCE"),
    MEMBER(4,"MEMBER"),
    OTC(5,"OTC"),
    SYSTEM(6,"SYSTEM"),
    PROMOTION(7,"PROMOTION"),
    INDEX(8,"INDEX"),
	ACTIVITY(9,"ACTIVITY"),
	CTC(10,"CTC"),
	REDENVELOPE(11,"REDENVELOPE"),
    CONTRACTOPTION(12,"CONTRACTOPTION");

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    AdminModule(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static AdminModule creator(Object v) {
        if(v instanceof String){
            for (AdminModule value : AdminModule.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (AdminModule value : AdminModule.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
