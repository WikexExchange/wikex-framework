/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: LoginHandler.java</p>
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
import com.wikex.wikex.core.common.constant.CommonConstant;
import com.wikex.wikex.core.common.constant.NettyCommands;
import com.wikex.wikex.core.common.constant.NettyResponseCode;
import com.wikex.wikex.core.entity.HawkResponseMessage;
import com.wikex.wikex.core.entity.LoginMessage;
import com.wikex.wikex.core.exception.NettyException;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * <p>
 * Title: LoginHandler
 * </p>
 * <p>
 * Description:
 * </p>
 * Login control request: first establish a connection and obtain the sessionId,
 * then initiate authentication. All subsequent requests need to carry the
 * sessionId.
 * 
 * @Author MrGao
 * @Date 2019-07-24
 */
@HawkBean
public class LoginHandler {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * <p>
	 * Title: login
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * The channel is cached only after a successful login.
	 * Connections that have not logged in successfully will not receive push
	 * messages.
	 * 
	 * @param seqId Sequence number
	 * @param body  Message body
	 * @param ctx   Data channel
	 * @return Response content
	 */
	@HawkMethod(cmd = NettyCommands.LOGIN, version = NettyCommands.COMMANDS_VERSION)
	public HawkResponseMessage.CommonResult login(long seqId, byte[] body, ChannelHandlerContext ctx) {
		Subject subject = SecurityUtils.getSubject();
		LoginMessage.LoginUser user;
		try {
			user = LoginMessage.LoginUser.newBuilder().mergeFrom(body).build();
			UsernamePasswordToken token = new UsernamePasswordToken(
					user.getUsername(), user.getPasswd());
			subject.login(token); // login

		} catch (InvalidProtocolBufferException e) {
			logger.error(NettyResponseCode.BODY_FORMAT_ERROR.getResponseString());
			throw new NettyException(e, NettyResponseCode.BODY_FORMAT_ERROR.getResponseString());
		} catch (UnknownAccountException | IncorrectCredentialsException e) {
			throw new NettyException(e, NettyResponseCode.LOGIN_AUTH_ERROR.getResponseCode()
					+ "~" + NettyResponseCode.LOGIN_AUTH_ERROR.getResponseMessage());
		}

		Session session = subject.getSession();
		session.setAttribute(CommonConstant.LOGINUSER, subject.getPrincipal());
		String userName = Objects.toString(subject.getPrincipal());
		String channelId = ctx.channel().id().asLongText();
		logger.info("[{}] User logged in successfully, caching Channel and Session information, IDs are: [{}], [{}]",
				userName, channelId, session.getId());

		return HawkResponseMessage.CommonResult.newBuilder()
				.setResultCode(NettyResponseCode.SUCCESS.getResponseCode())
				.setResultMsg(NettyResponseCode.SUCCESS.getResponseMessage())
				.build();
	}
}
