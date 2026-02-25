/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: DefaultHawkSecurityManager.java</p>
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
import com.wikex.wikex.netty.shiro.session.DefaultHawkSessionContext;
import com.wikex.wikex.netty.shiro.subject.DefaultHawkSubjectContext;
import com.wikex.wikex.netty.shiro.subject.HawkSubjectContext;
import com.wikex.wikex.netty.shiro.util.HawkUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.SubjectContext;

import java.io.Serializable;

/**
 * <p>Title: DefaultHawkSecurityManager</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019-07-24
 */
public class DefaultHawkSecurityManager  extends DefaultSecurityManager implements SecurityManager{

	@Override
    protected SubjectContext copy(SubjectContext subjectContext) {
        if (subjectContext instanceof HawkSubjectContext) {
            return new DefaultHawkSubjectContext((HawkSubjectContext) subjectContext);
        }
        return super.copy(subjectContext);
    }
	@Override
    protected SessionContext createSessionContext(SubjectContext subjectContext) {
        SessionContext sessionContext = super.createSessionContext(subjectContext);
        if (subjectContext instanceof HawkSubjectContext) {
            HawkSubjectContext wsc = (HawkSubjectContext) subjectContext;
            RequestPacket request = wsc.resolveHawkRequest();
            ResponsePacket response = wsc.resolveHawkResponse();
            DefaultHawkSessionContext webSessionContext = new DefaultHawkSessionContext(sessionContext);
            if (request != null) {
                webSessionContext.setHawkRequest(request);
            }
            if (response != null) {
                webSessionContext.setHawkResponse(response);
            }

            sessionContext = webSessionContext;
        }
        return sessionContext;
    }
	@Override
    protected SessionKey getSessionKey(SubjectContext context) {
        Serializable sessionId = context.getSessionId();
        RequestPacket request = HawkUtils.getRequest(context);
        ResponsePacket response = HawkUtils.getResponse(context);
        return new HawkSessionKey(sessionId, request, response);

    }

}
