package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum OrderStatus implements Serializable {

    
    CANCELLED(0, "Canceled"),

    NONPAYMENT(1, "Unpaid"),
    
    PAID(2, "Paid"),
    
    COMPLETED(3, "Completed"),
    
    APPEAL(4, "In Appeal");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    OrderStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static OrderStatus creator(Object v) {
        if(v instanceof String){
            for (OrderStatus value : OrderStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (OrderStatus value : OrderStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
