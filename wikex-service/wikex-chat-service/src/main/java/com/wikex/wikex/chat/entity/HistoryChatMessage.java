package com.wikex.wikex.chat.entity;

import lombok.Data;
import lombok.ToString;

/**
 * Parameter specification when the client requests historical messages
 */

@Data
@ToString(callSuper = true)
public class HistoryChatMessage extends BaseMessage{

    private int limit = 20 ;

    private String sortFiled = "sendTime";

    private int page = 1;

}
