/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 *
 * <p>FileName: HawkServerRealHandler.java</p>
 *
 * Description:
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.server;

import com.wikex.wikex.netty.common.NettyCacheUtils;
import com.wikex.wikex.service.ChannelEventDealService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * <p>
 * Title: HawkServerRealHandler
 * </p>
 * <p>
 * Description:
 * </p>
 *
 * @Author MrGao
 * @Date 2019-07-24
 */
@Sharable
public class HawkServerRealHandler extends HawkServerHandler {
	private static final Logger logger = LoggerFactory.getLogger(HawkServerRealHandler.class);
	@Autowired
	private ChannelEventDealService channelEventDealService;

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
//			if (IdleState.READER_IDLE == event.state()) { // Read idle
//				// logger.info("id:0x{}，read idle", ctx.channel().id().asLongText());
//			} else if (IdleState.WRITER_IDLE == event.state()) { // Write idle
//				// logger.info("id:0x{}，write idle", ctx.channel().id().asLongText());
//			} else if (IdleState.ALL_IDLE == event.state()) { // Read/Write idle
//				// logger.info("id:0x{}，read/write idle", ctx.channel().id().asLongText());
//				ctx.close(
//						new DefaultChannelPromise(ctx.channel()).addListener((ChannelFutureListener) channelFuture -> {
//							logger.error("timeoutClose");
//							// logger.info("channel[0x{}] timeout closed", ctx.channel().id().asLongText());
//						}));
//			}

			if (IdleState.ALL_IDLE == event.state()) {
				ctx.close(
					new DefaultChannelPromise(ctx.channel()).addListener((ChannelFutureListener) channelFuture -> {
						logger.error("timeoutClose");
					}));
			}
		}
	}

	// Called when the client and server create a connection
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		try {
			InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			InetSocketAddress localAddress = (InetSocketAddress)ctx.channel().localAddress();
			// logger.info("channel[{}] from {}:{} actived.", ctx.channel().id().asLongText(), remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
			// logger.info("--------------------Client and server connection opened-------------------------");
			channelEventDealService.dealChannelActive(localAddress.getAddress().getHostAddress(),remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * When the connection is disconnected
	 */
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx){
		try {
			InetSocketAddress remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
			InetSocketAddress localAddress = (InetSocketAddress)ctx.channel().localAddress();
			Channel channel = ctx.channel();
			// logger.info("channel[{}] from {}:{} disconnected.", ctx.channel().id().asLongText(), remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
			String user =  NettyCacheUtils.keyChannelCache.get(channel);
			if(user!=null){ // Push exists
				// logger.info("remove the push request from memory");
				Set<Channel> channels = NettyCacheUtils.getChannel(user);
				if(!CollectionUtils.isEmpty(channels)){ // Push established directly via push_request exists
					boolean flag = channels.remove(channel);
					// logger.info("user[{}] channel remove :"+flag,user);
				}
				Set<String> keys  = NettyCacheUtils.userKey.get(user);
				if(!CollectionUtils.isEmpty(keys)){
					// logger.info("need remove keys,total[{}]",keys.size());
					keys.forEach(key->{
						Set<Channel> keyChannels = NettyCacheUtils.getChannel(key);
						if(!CollectionUtils.isEmpty(keyChannels)){ // Push established directly via push_request exists
							boolean flag = keyChannels.remove(channel);
							// logger.debug("key[{}] channel remove :"+flag,key);
						}
					});
				}

			}
			channelEventDealService.dealChannelDestory(localAddress.getAddress().getHostAddress(),remoteAddress.getAddress().getHostAddress(), remoteAddress.getPort());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Send a message to the client
	 *
	 * @param username Username
	 * @param cmd Command name
	 * @return Returns true if the terminal is online, otherwise false
	 */
	public static int writeAndFlush(String username, short cmd, byte[] body) {
		return 1;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Channel channel = ctx.channel();
		ctx.close(new DefaultChannelPromise(ctx.channel()).addListener((ChannelFutureListener) channelFuture ->
				logger.error("exception-"+cause.getMessage()+" caused the channel to close")));
	}

}
