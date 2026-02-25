/*
 * Copyright (c) 2017-2018 All Rights Reserved.
 * @Author: sanfeng
 * @Date: 2018/3/19 10:51
 * @Version: 1.0
 * History:
 * v1.0.0, sanfeng,  2018/3/19 10:51, Create
 */
package com.wikex.wikex.netty.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.List;

public class WebSocketFrameDecoder extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) {
        ByteBuf buff = msg.content();
        byte[] messageBytes = new byte[buff.readableBytes()];
        buff.readBytes(messageBytes);
        ByteBuf bytebuf = PooledByteBufAllocator.DEFAULT.buffer();
        bytebuf.writeBytes(messageBytes);
        out.add(bytebuf.retain());
    }
}
