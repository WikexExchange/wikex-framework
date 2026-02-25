package com.wikex.wikex.admin.config;

import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.Serializable;

/**
 * Purpose: Shiro session management.
 *          Custom session rules to support frontend-backend separation. Use token-based login validation
 *          in cross-domain scenarios; otherwise, this class is not necessary.
 *          By default, Shiro uses ServletContainerSessionManager for session management, which relies on
 *          browser cookies to maintain sessions, calling storeSessionId to save the sessionId in a cookie.
 *          To support stateless sessions, extend DefaultWebSessionManager.
 *          To customize sessionId generation, implement SessionIdGenerator.
 * Notes:
 */
public class ShiroSession extends DefaultWebSessionManager {

    /**
     * The header key used to carry the token in requests.
     */
    private static final String AUTH_TOKEN = "admin-auth-token";

    private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless request";

    public ShiroSession() {
        super();
        // Set Shiro session timeout. Default is 30 minutes; here we set it to 60 minutes.
        setGlobalSessionTimeout(MILLIS_PER_MINUTE * 60);
    }

    /**
     * Obtain the sessionId (originally obtained via sessionKey).
     * The overridden part additionally sets the obtained token into the request.
     * When the app calls the login API, it has no token; after successful login a token is created.
     * We place it into the request so that when returning the result to the client, it can be retrieved
     * and sent by the client on subsequent requests—serving a role similar to a browser cookie—to maintain the session.
     *
     * @param request  ServletRequest
     * @param response ServletResponse
     * @return sessionId
     */
    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        // Get the value of AUTH_TOKEN from the request header; if present, its value is the sessionId.
        // Shiro controls sessions via sessionId.
        String sessionId = WebUtils.toHttp(request).getHeader(AUTH_TOKEN);
        System.out.println("--------------sessionid-----------" + sessionId);
        if (StringUtils.isEmpty(sessionId)) {
            // If no id is carried, fall back to the parent behavior to obtain sessionId from the cookie.
            return super.getSessionId(request, response);
        } else {
            // If the request header has auth token, its value is the sessionId.
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, REFERENCED_SESSION_ID_SOURCE);
            // sessionId
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, sessionId);
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
            return sessionId;
        }
    }
}