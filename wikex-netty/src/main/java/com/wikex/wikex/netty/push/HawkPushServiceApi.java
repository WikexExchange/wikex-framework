/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * <p>
 * <p>FileName: HawkPushServiceApi.java</p>
 * <p>
 * Description:
 *
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.push;

import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;

import java.util.Map;
import java.util.Set;

/**
 * <p>Title: HawkPushServiceApi</p>
 * <p>Description: </p>
 * Message push interface
 * @author MrGao
 * @date 
 */
public interface HawkPushServiceApi {
    /**
     *
     * <p>Title: pushMsg</p>
     * <p>Description: </p>
     * Push a text message to the client
     */
    void pushMsg(Set<Channel> channels, short cmd, String msg);

    /**
     *
     * <p>Title: pushMsg</p>
     * <p>Description: </p>
     * Push a byte message to the client
     */
    void pushMsg(Set<Channel> channels, short cmd, byte[] msg);

    /**
     *
     * <p>Title: pushMsg</p>
     * <p>Description: </p>
     * Push a Protobuf message to the client
     */
    void pushMsg(Set<Channel> channels, short cmd, MessageLite msg);

    /**
     *
     * <p>Title: pushMsg</p>
     * <p>Description: </p>
     * Push a text message to the client
     */
    Map<String,ChannelPromise> syncPushMsg(Set<Channel> channels, short cmd, String msg);

    /**
     *
     * <p>Title: pushMsg</p>
     * <p>Description: </p>
     * Push a byte message to the client
     */
    Map<String,ChannelPromise> syncPushMsg(Set<Channel> channels, short cmd, byte[] msg);

    /**
     *
     * <p>Title: pushMsg</p>
     * <p>Description: </p>
     * Push a Protobuf message to the client
     */
    Map<String,ChannelPromise> syncPushMsg(Set<Channel> channels, short cmd, MessageLite msg);
}
