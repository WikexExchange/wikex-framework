package com.wikex.wikex.p2p.entity.chat;

import lombok.Data;
import lombok.ToString;

/**
 * Unified base class format for chat messages
 */
@Data
@ToString
public class BaseMessage {

    private String orderId;

    private String uidTo;

    private String uidFrom;

    private String nameTo;

    private String nameFrom;

}
