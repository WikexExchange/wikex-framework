/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkSessionContext.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.shiro.mgt;

import com.wikex.wikex.core.entity.RequestPacket;
import com.wikex.wikex.core.entity.ResponsePacket;
import com.wikex.wikex.netty.shiro.util.RequestPairSource;
import org.apache.shiro.session.mgt.SessionContext;

/**
 * <p>Title: HawkSessionContext</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019-07-24
 */
public interface HawkSessionContext extends SessionContext, RequestPairSource {
	 RequestPacket getHawkRequest();
	 void setHawkRequest(RequestPacket request);
	 ResponsePacket getHawkResponse();
	 void setHawkResponse(ResponsePacket response);
}
