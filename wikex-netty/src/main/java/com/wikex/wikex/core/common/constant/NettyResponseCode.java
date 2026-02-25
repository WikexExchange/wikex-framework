/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 *
 * <p>FileName: NettyResponseCode.java</p>
 *
 * Description:
 * @author MrGao
 * @date July 19, 2019
 * @version 1.0
 * History:
 * v1.0.0, July 19, 2019, Create
 */
package com.wikex.wikex.core.common.constant;

/**
 * <p>
 * Title: NettyResponseCode
 * </p>
 * <p>
 * Description:
 * </p>
 * Response code configuration class.
 * Response codes are divided into two types:
 * <ol>
 * <li>
 * Request response codes: temporarily set as 200, 201, and 500.
 * That is, either success or failure.
 * On success, the body is serialized as a normal object.
 * On failure or asynchronous operation success, it is serialized according to
 * commresult,
 * to obtain the specific error code and reason.
 * </li>
 * <li>
 * Business error return codes: correspond to specific exception codes and
 * messages.
 * </li>
 * </ol>
 * 
 * @author MrGao
 * @date July 19, 2019
 */
public class NettyResponseCode {

	// ====================== Request Response Codes ==============================
	/**
	 * Normal status
	 */
	public static final NettyResponseBean SUCCESS = new NettyResponseBean(200, "Operation successful");
	/**
	 * Asynchronous request executed successfully
	 */
	public static final NettyResponseBean ASYNC_SUCCESS = new NettyResponseBean(201,
			"Asynchronous request executed successfully");
	/**
	 * Request execution error
	 */
	public static final NettyResponseBean REQUEST_ERROR = new NettyResponseBean(502, "Request execution error");

	// ====================== Business Error Return Codes =========================
	/**
	 * Unknown error
	 */
	public static final NettyResponseBean UNKNOW_ERROR = new NettyResponseBean(500, "Unknown error");

	/**
	 * Protobuffer body format error
	 */
	public static final NettyResponseBean BODY_FORMAT_ERROR = new NettyResponseBean(501,
			"Protobuffer body format error");
	/**
	 * Command not found
	 */
	public static final NettyResponseBean CMD_NOT_FOUND = new NettyResponseBean(404, "Command not found");
	/**
	 * Deprecated method
	 */
	public static final NettyResponseBean OBSOLETED_METHOD = new NettyResponseBean(405, "Deprecated method");
	/**
	 * UTF-8 encoding error
	 */
	public static final NettyResponseBean UTF8_ENCODING_ERROR = new NettyResponseBean(503,
			"Content cannot be converted to UTF-8");
	/**
	 * Not logged in
	 */
	public static final NettyResponseBean NOLOGIN_ERROR = new NettyResponseBean(504,
			"User session expired, please log in again!");
	/**
	 * Login failed, incorrect username or password
	 */
	public static final NettyResponseBean LOGIN_AUTH_ERROR = new NettyResponseBean(505,
			"Login failed, incorrect username or password!");

	/**
	 * No handler found for the request
	 */
	public static final NettyResponseBean NO_HANDLER_ERROR = new NettyResponseBean(506,
			"No handler found for the request");

	/**
	 * Filter IO exception
	 */
	public static final NettyResponseBean FILTER_IO_ERROR = new NettyResponseBean(507, "Filter IO exception");

	/**
	 * Handler access permission error
	 */
	public static final NettyResponseBean HANDLER_ACCESS_ERROR = new NettyResponseBean(508,
			"Handler access permission error");
	/**
	 * Handler access parameter error
	 */
	public static final NettyResponseBean HANDLER_ARGUMENT_ERROR = new NettyResponseBean(509,
			"Handler access parameter error");
	/**
	 * Handler invocation exception error
	 */
	public static final NettyResponseBean HANDLER_INVOCATE_ERROR = new NettyResponseBean(510,
			"Handler invocation exception error");

}
