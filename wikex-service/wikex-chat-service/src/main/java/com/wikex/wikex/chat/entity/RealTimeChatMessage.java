package com.wikex.wikex.chat.entity;

import lombok.Data;
import lombok.ToString;

/**
 * Parameter specification when the client sends real-time messages
 */
@Data
@ToString(callSuper = true)
public class RealTimeChatMessage extends BaseMessage {
    // Message content
    private String content;
    // Avatar of the message sender
    private String avatar;
}
