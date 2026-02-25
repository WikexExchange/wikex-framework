/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkSubjectContext.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.shiro.subject;

import com.wikex.wikex.core.entity.RequestPacket;
import com.wikex.wikex.core.entity.ResponsePacket;
import com.wikex.wikex.netty.shiro.util.RequestPairSource;
import org.apache.shiro.subject.SubjectContext;

/**
 * <p>Title: HawkSubjectContext</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019-07-24
 */
public interface HawkSubjectContext extends SubjectContext,RequestPairSource {

	 RequestPacket getHawkRequest();

	 void setHawkRequest(RequestPacket request);

	 RequestPacket resolveHawkRequest();

	 ResponsePacket getHawkResponse();

	 void setHawkResponse(ResponsePacket response);

	 ResponsePacket resolveHawkResponse();
}
