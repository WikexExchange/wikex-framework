
package com.wikex.wikex.core.filter;


import com.wikex.wikex.core.annotation.HawkFilterValue;
import com.wikex.wikex.core.annotation.HawkMethodHandler;
import com.wikex.wikex.core.common.constant.NettyResponseCode;
import com.wikex.wikex.core.entity.RequestPacket;
import com.wikex.wikex.core.entity.ResponsePacket;
import com.wikex.wikex.core.exception.NettyException;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * <p>Title: DefaultFilterChain</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 
 */
public class DefaultFilterChain implements FilterChain {
	
	private List<HawkFilterValue> filters = new ArrayList<HawkFilterValue>();
	private HawkMethodHandler handler;
	private int _iter=0;
	private boolean handlerExecFlag = false;

	public DefaultFilterChain(  TreeSet<HawkFilterValue> treeFilters,HawkMethodHandler handler) {
		this.handler = handler;
		for (HawkFilterValue filterValue : treeFilters) {
			filters.add(filterValue);
		}
	}

	@Override
	public void doFilter(RequestPacket request, ResponsePacket response, ChannelHandlerContext ctx) {
		if(CollectionUtils.isEmpty(filters)){
			if(handler==null){
				throw new NettyException(NettyResponseCode.NO_HANDLER_ERROR.getResponseCode()
						+"~"+ NettyResponseCode.NO_HANDLER_ERROR.getResponseMessage());
			}
			handler.doInvoke(request, response, ctx);
			handlerExecFlag=true;
			return;
		}
		try {
			HawkFilterValue filterValue;
			for(;_iter<filters.size();_iter++){
				if((filterValue = filters.get(_iter)).getHfilter().isMatch(request)){
					_iter++;
					HFilter hFilter = filterValue.getHfilter();
					hFilter.doFilter(request, response,ctx, this);
					break;
				}
			}
			
			if(_iter==filters.size() && !handlerExecFlag){
				handler.doInvoke(request, response, ctx);
				handlerExecFlag=true;
			}
		} catch ( IOException e) {
			throw new NettyException(NettyResponseCode.FILTER_IO_ERROR.getResponseCode()
					+"~"+e.getMessage());
		} catch ( RuntimeException e) {
			throw e;
		} catch ( Exception e) {
			throw new NettyException(NettyResponseCode.UNKNOW_ERROR.getResponseCode()
					+"~"+ NettyResponseCode.UNKNOW_ERROR.getResponseMessage());
		}
	}
}
