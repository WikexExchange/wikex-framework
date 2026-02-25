/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: ServerStatus.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-06-26
 * @version 1.0
 * History:
 * v1.0.0, 2019-06-26, Create
 */
package com.wikex.wikex.netty.common;

/**
 * <p>
 * Title: ServerStatus
 * </p>
 * <p>
 * Description:
 * </p>
 * Enumeration of server status states, divided into:<br/>
 * Default: DEFAULT(0);
 * Initializing: INIT(1);
 * Available: ALIVE(2);
 * Unavailable: DEAD(3);
 * Closed: CLOSE(4)
 * 
 * @author MrGao
 * @date 2019-06-26
 */
public enum ServerStatus {
	DEFAULT(0),
	INIT(1),
	ALIVE(2),
	DEAD(3),
	CLOSE(4);

	public final int value;

	private ServerStatus(int value) {
		this.value = value;
	}

	/**
	 * <p>
	 * Title: isClose
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * Check if the current value represents a closed state.
	 * 
	 * @return true if closed, false otherwise
	 */
	public boolean isClose() {
		return this == CLOSE;
	}

	/**
	 * <p>
	 * Title: isAlive
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * Check if the current value represents an active state.
	 * 
	 * @return true if active, false otherwise
	 */
	public boolean isAlive() {
		return this == ALIVE;
	}
}
