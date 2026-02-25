package com.wikex.wikex.chat.entity;

import lombok.Data;
import lombok.ToString;

/**
 * Unified parent class specification format for chat messages
 */

@Data
@ToString
public class BaseMessage {

    private String orderId;

    private String uidTo;

    private String uidFrom;

    private String nameTo;

    private String nameFrom;

    private MessageTypeEnum messageType = MessageTypeEnum.NORMAL_CHAT;
}
