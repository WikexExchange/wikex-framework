/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkRequestDispatcher.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-19
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-19, Create
 */
package com.wikex.wikex.netty.dispatcher;

import com.wikex.wikex.core.Dispatcher;
import com.wikex.wikex.core.annotation.HawkMethodHandler;
import com.wikex.wikex.core.common.constant.NettyResponseCode;
import com.wikex.wikex.core.context.HawkContext;
import com.wikex.wikex.core.entity.HawkResponseMessage;
import com.wikex.wikex.core.entity.RequestPacket;
import com.wikex.wikex.core.entity.ResponsePacket;
import com.wikex.wikex.core.exception.NettyException;
import com.wikex.wikex.core.filter.DefaultFilterChain;
import com.wikex.wikex.core.filter.FilterChain;
import com.google.common.base.Throwables;
import com.google.protobuf.InvalidProtocolBufferException;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * Title: HawkRequestDispatcher
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * @author MrGao
 * @date 2019-07-19
 */
public class HawkRequestDispatcher implements Dispatcher<RequestPacket, ResponsePacket> {
	private final Logger logger = LoggerFactory.getLogger(HawkRequestDispatcher.class);
	@Autowired
	private HawkContext hawkContext;
	
	@Override
	public ResponsePacket dispatch(RequestPacket request, ChannelHandlerContext ctx) throws NettyException {
		// Get the handler
		HawkMethodHandler HawkMethodHandler = hawkContext.getHawkMethodHandler(request.getCmd(), request.getVersion());
		ResponsePacket response = new ResponsePacket();
		response.setSequenceId(request.getSequenceId());
		response.setRequestId(request.getRequestId());
		response.setCmd((short) (request.getCmd())); // Terminal request command + 1000 to return
		if (HawkMethodHandler == null) {
			logger.error("Command {}#{} does not exist", request.getCmd(), request.getVersion());
			response.setCode(NettyResponseCode.CMD_NOT_FOUND.getResponseCode());
			response.setBody(HawkResponseMessage.CommonResult.newBuilder()
					.setResultCode(NettyResponseCode.CMD_NOT_FOUND.getResponseCode())
					.setResultMsg(NettyResponseCode.CMD_NOT_FOUND.getResponseMessage()).build().toByteArray());
			return response;
		}
		if (HawkMethodHandler.getHawkMethodValue().isObsoleted()) {
			logger.error("Command {}#{} has expired", request.getCmd(), request.getVersion());
			response.setCode(NettyResponseCode.OBSOLETED_METHOD.getResponseCode());
			response.setBody(HawkResponseMessage.CommonResult.newBuilder()
					.setResultCode(NettyResponseCode.OBSOLETED_METHOD.getResponseCode())
					.setResultMsg(NettyResponseCode.OBSOLETED_METHOD.getResponseMessage()).build().toByteArray());
			return response;
		}
		return doInvoke(request, ctx, HawkMethodHandler, response);
	}

	private ResponsePacket doInvoke(RequestPacket request, ChannelHandlerContext ctx,
			HawkMethodHandler hawkMethodHandler, ResponsePacket response) {

		try {
			FilterChain chain = new DefaultFilterChain(hawkContext.getFilters(), hawkMethodHandler);
			chain.doFilter(request, response, ctx);
		} catch (RuntimeException e) { // The content of hawkException is in the format: [code-message], can be directly converted into a common return
			e.printStackTrace();
			logger.error("Command {}#{} business exception, message={}", request.getCmd(), request.getVersion(),
					e.getMessage());
			response.setCode(NettyResponseCode.REQUEST_ERROR.getResponseCode());
			buildExceptionBody(response, e.getMessage());
		} catch (Exception e) {
			response.setCode(NettyResponseCode.REQUEST_ERROR.getResponseCode());
			if (e instanceof InvalidProtocolBufferException || e.getCause() instanceof InvalidProtocolBufferException) {
				logger.error("Command {}#{} packet format error, {}", request.getCmd(), request.getVersion(),
						Throwables.getStackTraceAsString(e));
				response.setBody(HawkResponseMessage.CommonResult.newBuilder()
					.setResultCode(NettyResponseCode.BODY_FORMAT_ERROR.getResponseCode())
					.setResultMsg(e.getMessage()).build().toByteArray());
			} else {
				logger.error("Command {}#{} unknown error, {}", request.getCmd(), request.getVersion(),
						Throwables.getStackTraceAsString(e), e);
				response.setBody(HawkResponseMessage.CommonResult.newBuilder()
					.setResultCode(NettyResponseCode.UNKNOW_ERROR.getResponseCode())
					.setResultMsg(e.getMessage()).build().toByteArray());
			}
		}
		return response;
	}

	/**
	 * 
	 * <p>Title: buildExceptionBody</p>
	 * <p>Description: </p>
	 * @param response
	 * @param message
	 */
	private void buildExceptionBody(ResponsePacket response, String message) {
		String[] results = message.split("~");
		response.setBody(HawkResponseMessage.CommonResult.newBuilder().setResultCode(Integer.parseInt(results[0]))
				.setResultMsg(results[1]).build().toByteArray());
	}
	
}
