/*
 * Copyright (c) 2017-2018  All Rights Reserved.
 *
 * <p>FileName: NettyServer.java</p>
 *
 * Description:
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.server;

import com.wikex.wikex.core.exception.NettyException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * <p>
 * Title: NettyServer
 * </p>
 * <p>
 * Description:
 * </p>
 *
 * @Author MrGao
 * @Date 2019-07-24
 */
public class NettyServer implements Server, Runnable {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private int port ;
    private int bossThreadSize;
    private int workerThreadSize;
    private ChannelInitializer channelInitializer;

    NettyServer(int port ,int bossThreadSize,int workerThreadSize, ChannelInitializer channelInitializer) {
        this.port = port;
        this.bossThreadSize = bossThreadSize;
        this.workerThreadSize = workerThreadSize;
        this.channelInitializer = channelInitializer;
    }

    @Override
    public void open() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadSize);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadSize);
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                // Set the size of the connection backlog buffer
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_RCVBUF, 1024 * 1024)
                .childOption(ChannelOption.SO_SNDBUF, 10 * 1024 * 1024)
                // Keep the connection alive and remove dead connections
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // Set memory object pool
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                // Disable delayed sending (send immediately)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(this.channelInitializer);
        InetSocketAddress localAddress = new InetSocketAddress(port);
        try {
            // Bind the port and wait synchronously
            ChannelFuture f = b.bind(localAddress).sync();
            logger.info("Server started at port {}", localAddress.getPort());
            Channel serverChannel = f.channel();
            // Wait until the listening port is closed
            serverChannel.closeFuture().sync();
        } catch (InterruptedException e) {

            throw new NettyException(e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void run() {
        this.open();
    }

    /*
     * (non-Javadoc) <p>Title: close</p> <p>Description: </p>
     *
     * @see com.spark.hawk.server.Server#close()
     */
    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc) <p>Title: isClosed</p> <p>Description: </p>
     *
     * @return
     *
     * @see com.spark.hawk.server.Server#isClosed()
     */
    @Override
    public boolean isClosed() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc) <p>Title: isAvailable</p> <p>Description: </p>
     *
     * @return
     *
     * @see com.spark.hawk.server.Server#isAvailable()
     */
    @Override
    public boolean isAvailable() {
        // TODO Auto-generated method stub
        return false;
    }


}
