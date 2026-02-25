/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HeartBeatHandler.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.handler;

import com.wikex.wikex.core.annotation.HawkBean;
import com.wikex.wikex.core.annotation.HawkMethod;
import com.wikex.wikex.core.common.constant.NettyCommands;
import com.wikex.wikex.core.common.constant.NettyResponseCode;
import com.wikex.wikex.core.entity.HawkResponseMessage;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Title: HeartBeatHandler
 * </p>
 * <p>
 * Description:
 * </p>
 * Heartbeat packet processing request. Due to the characteristics of mobile
 * wireless networks,
 * the heartbeat cycle of the push service cannot be set too long, otherwise the
 * long connection
 * will be released, causing frequent client reconnections. However, it also
 * cannot be set too short,
 * otherwise, in the absence of a unified heartbeat framework mechanism, it can
 * easily cause a signaling storm
 * (for example, the WeChat heartbeat signaling storm issue).
 * There is no unified standard for the specific heartbeat cycle; 180 seconds
 * may be a good choice,
 * while WeChat uses 300 seconds.
 * 
 * @Author MrGao
 * @Date 2019-07-24
 */
@HawkBean
public class HeartBeatHandler {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 *
	 * @param seqId Request ID
	 * @param body  Request body
	 * @param ctx   Channel
	 * @return Response
	 */
	@HawkMethod(cmd = NettyCommands.HEART_BEAT, version = NettyCommands.COMMANDS_VERSION)
	public HawkResponseMessage.CommonResult heartBeat(long seqId, byte[] body, ChannelHandlerContext ctx) {
		return HawkResponseMessage.CommonResult.newBuilder()
				.setResultCode(NettyResponseCode.SUCCESS.getResponseCode())
				.setResultMsg("")
				.build();
	}
}
