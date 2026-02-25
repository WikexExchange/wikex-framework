/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkPushServiceImpl.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.push.impl;

import com.wikex.wikex.core.common.constant.NettyResponseCode;
import com.wikex.wikex.core.entity.ResponsePacket;
import com.wikex.wikex.netty.push.HawkPushServiceApi;
import com.google.protobuf.MessageLite;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Title: HawkPushServiceImpl
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * @Author MrGao
 * @Date 2019-07-24
 */
public class HawkPushServiceImpl implements HawkPushServiceApi {
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	// Minimum sequence number on the server side
	private final static int MIN_SEQ_ID = 0x1fffffff;
	private static AtomicInteger idWoker = new AtomicInteger(MIN_SEQ_ID);

	@Override
	public void pushMsg(Set<Channel> channels, short cmd, String msg) {
		if (CollectionUtils.isEmpty(channels)) { // No users to push to, exit directly
			return;
		}
		Iterator<Channel> iterable = channels.iterator();
		while (iterable.hasNext()) {
			Channel channel = iterable.next();
			try {
				if (channel != null && channel.isActive()) {
					channel.writeAndFlush(buildResponsePacket(cmd, msg.getBytes()));
				} else {
					logger.debug("Push channel has been closed, removing this push channel");
					iterable.remove();
					logger.debug("Channel removal result: " + true);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

		}
	}

	@Override
	public void pushMsg(Set<Channel> channels, short cmd, byte[] msg) {
		if (channels == null || CollectionUtils.isEmpty(channels)) { // No users to push to, exit directly
			return;
		}
		try {
			Iterator<Channel> iterable = channels.iterator();
			while (iterable.hasNext()) {
				Channel channel = iterable.next();
				if (channel != null && channel.isActive()) {
					channel.writeAndFlush(buildResponsePacket(cmd, msg));
				} else {
					logger.debug("Push channel has been closed, removing this push channel");
					iterable.remove();
					logger.debug("Channel removal result: " + true);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void pushMsg(Set<Channel> channels, short cmd, MessageLite msg) {
		if (channels == null || CollectionUtils.isEmpty(channels)) { // No users to push to, exit directly
			return;
		}
		try {
			Iterator<Channel> iterable = channels.iterator();
			while (iterable.hasNext()) {
				Channel channel = iterable.next();
				if (channel != null && channel.isActive()) {
					channel.writeAndFlush(buildResponsePacket(cmd, msg.toByteArray()));
				} else {
					logger.debug("Push channel has been closed, removing this push channel");
					iterable.remove();
					logger.debug("Channel removal result: " + true);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public Map<String, ChannelPromise> syncPushMsg(Set<Channel> channels, short cmd, String msg) {
		if (CollectionUtils.isEmpty(channels)) { // No users to push to, exit directly
			return null;
		}
		Map<String, ChannelPromise> channelPromiseMap = new HashMap<>();
		Iterator<Channel> iterable = channels.iterator();
		while (iterable.hasNext()) {
			Channel channel = iterable.next();
			try {
				if (channel != null && channel.isActive()) {
					channelPromiseMap.put(channel.id().asLongText(),
							channel.writeAndFlush(buildResponsePacket(cmd, msg.getBytes())).channel().newPromise());
				} else {
					logger.debug("Push channel has been closed, removing this push channel");
					iterable.remove();
					logger.debug("Channel removal result: " + true);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return channelPromiseMap;
	}

	@Override
	public Map<String, ChannelPromise> syncPushMsg(Set<Channel> channels, short cmd, byte[] msg) {
		if (CollectionUtils.isEmpty(channels)) { // No users to push to, exit directly
			return null;
		}
		Map<String, ChannelPromise> channelPromiseMap = new HashMap<>();
		Iterator<Channel> iterable = channels.iterator();
		while (iterable.hasNext()) {
			Channel channel = iterable.next();
			try {
				if (channel != null && channel.isActive()) {
					channelPromiseMap.put(channel.id().asLongText(),
							channel.writeAndFlush(buildResponsePacket(cmd, msg)).channel().newPromise());
				} else {
					logger.debug("Push channel has been closed, removing this push channel");
					iterable.remove();
					logger.debug("Channel removal result: " + true);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return channelPromiseMap;
	}

	@Override
	public Map<String, ChannelPromise> syncPushMsg(Set<Channel> channels, short cmd, MessageLite msg) {
		if (CollectionUtils.isEmpty(channels)) { // No users to push to, exit directly
			return null;
		}
		Map<String, ChannelPromise> channelPromiseMap = new HashMap<>();
		Iterator<Channel> iterable = channels.iterator();
		while (iterable.hasNext()) {
			Channel channel = iterable.next();
			try {
				if (channel != null && channel.isActive()) {
					channelPromiseMap.put(channel.id().asLongText(),
							channel.writeAndFlush(buildResponsePacket(cmd, msg.toByteArray())).channel().newPromise());
				} else {
					logger.debug("Push channel has been closed, removing this push channel");
					iterable.remove();
					logger.debug("Channel removal result: " + true);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return channelPromiseMap;
	}

	/**
	 * <p>
	 * Title: buildResponsePacket
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * Build response packet
	 * 
	 * @param cmd  Command
	 * @param body Content
	 * @return Response content
	 */
	private ResponsePacket buildResponsePacket(short cmd, byte[] body) {
		ResponsePacket packet = new ResponsePacket();
		packet.setCmd(cmd);
		packet.setSequenceId(nextSeqId());
		packet.setCode(NettyResponseCode.SUCCESS.getResponseCode());
		packet.setBody(body);
		return packet;
	}

	/**
	 * <p>
	 * Unique sequence number. Minimum value {@link #MIN_SEQ_ID}, maximum value
	 * {@link Integer#MAX_VALUE}.
	 * Used to distinguish from the 9-digit sequence number generated by the client.
	 * </p>
	 * 
	 * @return Sequence number
	 */
	private static int nextSeqId() {
		int seqId = idWoker.getAndIncrement();
		// AtomicInteger will become negative after reaching 0x7fffffff
		while (seqId < MIN_SEQ_ID) {
			seqId = idWoker.addAndGet(MIN_SEQ_ID);
		}
		return seqId;
	}
}
