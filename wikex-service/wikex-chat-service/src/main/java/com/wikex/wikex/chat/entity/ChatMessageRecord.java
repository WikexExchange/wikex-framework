package com.wikex.wikex.chat.entity;

import lombok.Data;
import lombok.ToString;


@Data
@ToString(callSuper = true)
public class ChatMessageRecord extends BaseMessage{

    private String content ;

    private long sendTime ;

    private String sendTimeStr ;

    private String fromAvatar;

}
