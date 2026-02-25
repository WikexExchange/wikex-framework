
package com.wikex.wikex.core.filter;

import com.wikex.wikex.core.entity.RequestPacket;
import com.wikex.wikex.core.entity.ResponsePacket;
import io.netty.channel.ChannelHandlerContext;

/**
 * <p>Title: FilterChain</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 
 */
public interface FilterChain {
	
	public void doFilter(RequestPacket request, ResponsePacket response, ChannelHandlerContext ctx);
}
