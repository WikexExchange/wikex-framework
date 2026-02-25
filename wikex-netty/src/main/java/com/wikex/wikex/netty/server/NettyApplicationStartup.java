/*
 * Copyright (c) 2017-2018 Archimedes All Rights Reserved.
 * @Author: sanfeng
 * @Date: 2018/3/15 10:57
 * @Version: 1.0
 * History:
 * v1.0.0, sanfeng,  2018/3/15 10:57, Create
 */
package com.wikex.wikex.netty.server;

import com.wikex.wikex.core.configuration.NettyProperties;
import com.wikex.wikex.netty.websocket.WebSocketChannelInitializer;
import io.netty.channel.ChannelInitializer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import static com.wikex.wikex.core.core.common.NettySpringContextUtils.getApplicationContext;

/**
 * <p>Description: </p>
 *
 * @Author: sanfeng
 * @Date: 2018/3/15 10:57
 */

public class NettyApplicationStartup implements ApplicationRunner {
//    @Override
//    public void onApplicationEvent(ContextRefreshedEvent event) {
////        String displayName = event.getApplicationContext().getDisplayName();
////        if(displayName.startsWith("FeignContext")){
////            return;
////        }
//
//    }
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // After the container has loaded, obtain the DAO layer to operate the database
        NettyProperties nettyProperties = getApplicationContext().getBean(NettyProperties.class);
        // After the container has loaded, obtain configurations from the configuration file
        ChannelInitializer hawkServerInitializer = getApplicationContext().getBean(HawkServerInitializer.class);
        // After the container has loaded, start the thread
        Thread thread = new Thread(new NettyServer(nettyProperties.getPort(), nettyProperties.getBossThreadSize(),
                nettyProperties.getWorkerThreadSize(), hawkServerInitializer));
        thread.start();
        if (nettyProperties.getWebsocketFlag() == 1) { // WebSocket support is configured, start WebSocket service
            ChannelInitializer webSocketChannelInitializer = getApplicationContext().getBean(WebSocketChannelInitializer.class);
            Thread websocketThread = new Thread(new NettyServer(nettyProperties.getWebsocketPort(), nettyProperties.getBossThreadSize(),
                    nettyProperties.getWorkerThreadSize(), webSocketChannelInitializer));
            websocketThread.start();

        }
    }
}
