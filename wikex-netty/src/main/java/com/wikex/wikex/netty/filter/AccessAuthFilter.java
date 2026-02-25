/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: AccessAuthFilter.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.filter;

import com.wikex.wikex.core.annotation.HawkFilter;
import com.wikex.wikex.core.common.constant.NettyCommands;
import com.wikex.wikex.core.common.constant.NettyResponseCode;
import com.wikex.wikex.core.configuration.NettyProperties;
import com.wikex.wikex.core.entity.RequestPacket;
import com.wikex.wikex.core.entity.ResponsePacket;
import com.wikex.wikex.core.exception.NettyException;
import com.wikex.wikex.core.filter.FilterChain;
import com.wikex.wikex.core.filter.HFilter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>Title: AccessAuthFilter</p>
 * <p>Description: </p>
 * Access authentication filter, checks whether sessionId exists.
 * If it exists, checks whether the user is already logged in.
 * Otherwise, sends a login command back to the client.
 * @Author MrGao
 * @Date 2019-07-24
 */
//@HawkFilter(order = 2, ignoreCmds={NettyCommands.CONNECT, NettyCommands.LOGIN})
@HawkFilter(order = 2)
public class AccessAuthFilter extends HFilter {

	@Autowired
	private NettyProperties nettyProperties;

	@Override
	public void init() throws NettyException {
		
	}

	@Override
	public void doFilter(RequestPacket request, ResponsePacket response, ChannelHandlerContext ctx, FilterChain chain)
			throws NettyException {
		Subject subject = SecurityUtils.getSubject();
		if (nettyProperties.getDirectAccessFlag() != 1
				|| nettyProperties.getDirectAccessCommand() == null // Enable direct access
				|| !nettyProperties.getDirectAccessCommand().contains(String.valueOf(request.getCmd()))) { // Allowed access commands contain the requested command
			if (request.getCmd() != NettyCommands.LOGIN
					&& subject.getPrincipal() == null) { // Not logged in, prompt login
				Session session = subject.getSession();
				response.setSequenceId((long) session.getId()); // Return sequenceId to client
				throw new NettyException(buildExceptionMsg(NettyResponseCode.NOLOGIN_ERROR.getResponseCode(),
						NettyResponseCode.NOLOGIN_ERROR.getResponseMessage()));
			}
			if (request.getCmd() == NettyCommands.LOGIN) { // Special handling for login request to return sessionId once
				subject.getPrincipal(); // Must execute this to get a new session
				Session session = subject.getSession();
				request.setSequenceId((long) session.getId());
				response.setSequenceId((long) session.getId()); // Return sequenceId to client
			}
			chain.doFilter(request, response, ctx);
		}
	}

	@Override
	public void destroy() {
		
	}
}
