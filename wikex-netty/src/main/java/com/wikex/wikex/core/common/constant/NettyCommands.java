/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: Commands.java</p>
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
 * Title: Commands
 * </p>
 * <p>
 * Description:
 * </p>
 * Set of commands for interaction between client and server
 * <ol>
 * <li>Query: Default is a synchronous request, command segment number is 11, 
 * specific command is three digits, e.g., 11001, 11002</li>
 * <li>Asynchronous query: When query logic is more complex, asynchronous query can be chosen. 
 * Command segment number is 12, specific command is three digits, e.g., 12001, 12002. 
 * Query results are stored in a message queue, consumed by the server’s consumer, and after consumption, 
 * the channel is used to return data to the client.</li>
 * <li>Update: Default is an asynchronous request, command segment number is 22, 
 * specific command is three digits, e.g., 22001, 22002</li>
 * <li>Synchronous update: Update results synchronously</li>
 * </ol>
 * 
 * @author MrGao
 * @date July 19, 2019
 */
public class NettyCommands {
	public static final int COMMANDS_VERSION = 1;
	/**
	 * Connection request
	 */
	public static final short CONNECT = 11001;
	/**
	 * Login request
	 */
	public static final short LOGIN = 11002;
	/**
	 * JSON login request
	 */
	public static final short JSONLOGIN = 11000;
	/**
	 * Request to establish push channel
	 */
	public static final short PUSH_REQUEST = 11003;
	/**
	 * Heartbeat request
	 */
	public static final short HEART_BEAT = 11004;
}
