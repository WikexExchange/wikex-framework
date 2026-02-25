/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HandlerThreadDispatcher.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.server;

import com.wikex.wikex.core.Dispatcher;
import com.wikex.wikex.core.common.constant.NettyCommands;
import com.wikex.wikex.core.configuration.NettyProperties;
import com.wikex.wikex.core.entity.Packet;
import com.wikex.wikex.core.entity.RequestPacket;
import com.wikex.wikex.core.entity.ResponsePacket;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <p>Title: HandlerThreadDispatcher</p>
 * <p>Description: </p>
 * After receiving a request sent from the client, 
 * the decoded object will be passed to the business thread, 
 * and the business thread will handle the specific logic.
 * @Author MrGao
 * @Date 2019-07-24
 */
public class HandlerThreadDispatcher {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private ExecutorService executor;
	@Autowired
	public HandlerThreadDispatcher(NettyProperties nettyProperties){
		executor = Executors.newFixedThreadPool(nettyProperties.getDealHandlerThreadSize());
	}

	public void runByThread(ChannelHandlerContext ctx, RequestPacket msg, Dispatcher<RequestPacket,ResponsePacket> dispatcher){
		try{
			HandlerBusinessDealThread thread = new HandlerBusinessDealThread(ctx,msg,dispatcher);
			executor.submit(thread);
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}
	}
	public class HandlerBusinessDealThread implements Runnable {
		private Dispatcher<RequestPacket, ResponsePacket> dispatcher;
		private ChannelHandlerContext ctx;
		private RequestPacket packet;
		private HandlerBusinessDealThread(ChannelHandlerContext ctx, RequestPacket packet,Dispatcher<RequestPacket,ResponsePacket> dispatcher){
			this.ctx = ctx;
			this.packet = packet;
			this.dispatcher = dispatcher;
		}	
		@Override
		public void run() {
			Packet response = dispatcher.dispatch(packet, ctx);
	        if (packet.getCmd()!= NettyCommands.HEART_BEAT // Heartbeat requests do not get a response
				&& response != null) {
	            ctx.writeAndFlush(response, 
	            		new DefaultChannelPromise(ctx.channel())
	            		.addListener(
	            				(ChannelFutureListener) channelFuture -> responseComplete(packet))
	            		);
	        }	
		}
		/**
		 * 
		 * <p>Title: responseComplete</p>
		 * <p>Description: </p>
		 * Response completion event
		 * @param packet  Data packet
		 */
	    private void responseComplete(RequestPacket packet) {
			logger.info("Respone the request of seqId={},cmd={}",packet.getSequenceId(),packet.getCmd());
	    }
	}
}
