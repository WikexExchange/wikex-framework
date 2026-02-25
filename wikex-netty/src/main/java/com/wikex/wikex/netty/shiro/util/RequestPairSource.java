/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: RequestPairSource.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.shiro.util;


import com.wikex.wikex.core.entity.RequestPacket;
import com.wikex.wikex.core.entity.ResponsePacket;

/**
 * <p>Title: RequestPairSource</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019-07-24
 */
public interface RequestPairSource {

	RequestPacket getHawkRequest();

	ResponsePacket getHawkResponse();
}
