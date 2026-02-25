
package com.wikex.wikex.core.filter;

import com.wikex.wikex.core.annotation.HawkFilter;
import com.wikex.wikex.core.entity.RequestPacket;
import com.wikex.wikex.core.entity.ResponsePacket;
import com.wikex.wikex.core.exception.NettyException;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;

/**
 * <p>Title: Hfilter</p>
 * <p>Description: </p>
 * @Author MrGao
 * @Date 
 */
public abstract class HFilter {
	
	public abstract void init() throws NettyException;
	public abstract void doFilter (RequestPacket request, ResponsePacket response, ChannelHandlerContext ctx, FilterChain chain )
			 throws IOException, NettyException;
	public abstract void destroy();

	protected String buildExceptionMsg(int code,String message){
		return code+"~"+message;
	}

    public boolean isMatch(RequestPacket req) {
    	HawkFilter hawkFilter = this.getClass().getAnnotation(HawkFilter.class);
        for (int cmd : hawkFilter.ignoreCmds()) {
            if (cmd == req.getCmd()) {
                return false;
            }
        }
        for (int cmd : hawkFilter.cmds()) {
            if (cmd == req.getCmd()) {
                return true;
            }
        }
        return true;
    }
}
