package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;


public enum AuditStatus implements Serializable {

    AUDIT_ING(0, "Pending Review"),

    AUDIT_DEFEATED(1, "Review Failed"),
    
    AUDIT_SUCCESS(2, "Review Successful");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    AuditStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static AuditStatus creator(Object v) {
        if(v instanceof String){
            for (AuditStatus value : AuditStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (AuditStatus value : AuditStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
