package com.wikex.wikex.chat.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.io.Serializable;
import java.util.Objects;

public enum MessageTypeEnum implements Serializable {

    /**
     * Remind the other party to refresh the order page
     */
    NOTICE(0,"Confirm Payment"),
    /**
     * Chat
     */
    NORMAL_CHAT(1,"Normal Chat");

    @EnumValue
    private final int code;

    public int getCode() {
        return this.code;
    }

    public String getDescription() {
        return description;
    }

    private final String description;

    MessageTypeEnum(int val, String description) {
        this.code = val;
        this.description = description;
    }

    @JsonCreator
    public static MessageTypeEnum creator(Object v) {
        if(v instanceof String){
            for (MessageTypeEnum value : MessageTypeEnum.values()) {
                if (Objects.equals(v, value.name())) {
                    return value;
                }
            }
        } else {
            for (MessageTypeEnum value : MessageTypeEnum.values()) {
                if (Objects.equals(v, value.getCode())) {
                    return value;
                }
            }
        }
        return null;
    }
}
