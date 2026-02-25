package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Objects;


public enum CertifiedBusinessStatus implements Serializable {

    NOT_CERTIFIED(0, "Not Certified"),

    AUDITING(1, "Certification - Pending Review"), //1
    
    VERIFIED(2, "Certification - Approved"), //2
    
    FAILED(3, "Certification - Rejected"),  //3
    
    DEPOSIT_LESS(4, "Insufficient Deposit"), //4
    
    CANCEL_AUTH(5, "Cancellation - Pending Review"), //5
    
    RETURN_FAILED(6, "Cancellation - Rejected"), //6
    
    RETURN_SUCCESS(7, "Cancellation - Approved") //7
    
    ;

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    CertifiedBusinessStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static CertifiedBusinessStatus creator(Object v) {
        if(v instanceof String){
            for (CertifiedBusinessStatus value : CertifiedBusinessStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (CertifiedBusinessStatus value : CertifiedBusinessStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
              }
        }
        return null;
    }
}
