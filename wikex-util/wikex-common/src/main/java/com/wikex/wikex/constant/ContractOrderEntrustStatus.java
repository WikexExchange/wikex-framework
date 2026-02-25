package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;


public enum ContractOrderEntrustStatus implements Serializable {

    ENTRUST_ING(0, "Entrusting"),

    ENTRUST_CANCEL(1, "Canceled"),
    
    ENTRUST_FAILURE(2, "Entrust Failed"),
    
    ENTRUST_SUCCESS(3, "Entrust Successful");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    ContractOrderEntrustStatus(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static ContractOrderEntrustStatus creator(Object v) {
        if(v instanceof String){
            for (ContractOrderEntrustStatus value : ContractOrderEntrustStatus.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (ContractOrderEntrustStatus value : ContractOrderEntrustStatus.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
