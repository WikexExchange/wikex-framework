/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: Dispatcher.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-19
 * @version 1.0
 * History:
 * v1.0.0, 2019-07-19, Create
 */
package com.wikex.wikex.core;

import com.wikex.wikex.core.entity.Packet;
import com.wikex.wikex.core.exception.NettyException;
import io.netty.channel.ChannelHandlerContext;

/**
 * <p>Title: Dispatcher</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019-07-19
 */
public interface Dispatcher<R extends Packet, P extends Packet> {

    /**
     * Dispatch request
     * @param request Request packet
     * @param ctx Context, for the purpose of allowing lower-level business code to access it
     * @return Response packet
     * @throws NettyException if dispatch fails
     */
    P dispatch(R request, ChannelHandlerContext ctx) throws NettyException;
}
