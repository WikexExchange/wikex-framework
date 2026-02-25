package com.wikex.wikex.netty.server;

/**
 * 
 * <p>Title: Server</p>
 * <p>Description: Unified interface for server, defines common methods for servers</p>
 * @author MrGao
 * @date 2019-07-24
 */
public interface Server {
	/**
	 * 
	 * <p>Title: open</p>
	 * <p>Description: </p>
	 * Start the server, usually executed after loading the specific Server instance with Spring startup
	 */
	public void open();
	
	/**
	 * 
	 * <p>Title: close</p>
	 * <p>Description: </p>
	 * Disconnect the channel and exit.<br/>
	 * This method should be called to release resources when the specific Server instance is destroyed
	 */
	public void close();
	
	/**
	 * 
	 * <p>Title: isClosed</p>
	 * <p>Description: </p>
	 * Check whether the current instance's channel has already been closed
	 * @return
	 */
	public boolean isClosed();
	
	/**
	 * 
	 * <p>Title: isAvailable</p>
	 * <p>Description: </p>
	 * Check whether the current server is available
	 * @return
	 */
	public boolean isAvailable();
}
