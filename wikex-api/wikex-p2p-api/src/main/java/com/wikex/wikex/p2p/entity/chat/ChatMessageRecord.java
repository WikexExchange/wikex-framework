package com.wikex.wikex.p2p.entity.chat;

import lombok.Data;
import lombok.ToString;

/**
 * Format specification for storing chat messages in MongoDB
 */

@Data
@ToString(callSuper = true)
public class ChatMessageRecord extends BaseMessage{

    private String content ;

    private long sendTime ;

    private String sendTimeStr ;


}
