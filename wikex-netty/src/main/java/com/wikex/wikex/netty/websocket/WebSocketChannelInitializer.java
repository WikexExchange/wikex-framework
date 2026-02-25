/*
 * Copyright (c) 2017-2018  All Rights Reserved.
 * @Author: sanfeng
 * @Date: 2018/3/16 16:53
 * @Version: 1.0
 * History:
 * v1.0.0, sanfeng,  2018/3/16 16:53, Create
 */
package com.wikex.wikex.netty.websocket;

import com.wikex.wikex.core.configuration.NettyProperties;
import com.wikex.wikex.netty.codec.Codec;
import com.wikex.wikex.netty.codec.HawkServerDecoder;
import com.wikex.wikex.netty.codec.HawkServerEncoder;
import com.wikex.wikex.netty.server.HawkServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.stomp.StompSubframeDecoder;
import io.netty.handler.codec.stomp.StompSubframeEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <p>Description: </p>
 *
 * @Author: sanfeng
 * @Date: 2018/3/16 16:53
 */
@Component
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    private Codec codec;
    @Autowired
    private HawkServerHandler handler;
    @Autowired
    private NettyProperties nettyProperties;

    @Override
    protected void initChannel(SocketChannel ch) {

        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("logger", new LoggingHandler(LogLevel.WARN))
                .addLast("httpServerCodec", new HttpServerCodec())
                .addLast("chunkedWriteHandler", new ChunkedWriteHandler())
                .addLast("httpObjectAggregator", new HttpObjectAggregator(65536))
                .addLast("websocketDecoder",new WebSocketFrameDecoder())
                .addLast("stompDecoder",new StompSubframeDecoder())
                .addLast("decoder", new HawkServerDecoder(codec))
                .addLast("websocketEncoder",new WebSocketFramePrepender())
                .addLast("stompEncoder",new StompSubframeEncoder())
                .addLast("encoder", new HawkServerEncoder(codec))
                .addLast("idle", new IdleStateHandler(nettyProperties.getReaderIdle(), nettyProperties.getWriterIdle(), nettyProperties.getBothIdle()))
                .addLast("handler", handler);
    }
}