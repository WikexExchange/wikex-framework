package com.wikex.wikex.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Objects;


public enum AnnouncementClassification implements Serializable {

    NORMAL(0, "General Announcement"),

    ACTIVITY(1, "Activity Announcement"),
    
    ASSETS(2, "Deposit/Withdrawal Announcement"),
    
    SYSTEM(3, "System Announcement"),
    
    LIST(4, "Listing Announcement"),
    
    APPROVE(5, "Voting Announcement"),
    
    OTHER(6, "Other");
    

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    AnnouncementClassification(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static AnnouncementClassification creator(Object v) {
        if(v instanceof String){
            for (AnnouncementClassification value : AnnouncementClassification.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        }else {
            for (AnnouncementClassification value : AnnouncementClassification.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
