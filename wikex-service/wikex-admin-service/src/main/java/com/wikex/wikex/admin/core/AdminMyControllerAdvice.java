package com.wikex.wikex.admin.core;

import com.wikex.wikex.util.MessageResult;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice
public class AdminMyControllerAdvice {
    /**
     * Intercept and capture permissionless exceptions
     *
     * @param ex
     * @return
     */
    @ResponseBody
    @ExceptionHandler(value = AuthorizationException.class)
    public MessageResult handleAuthorizationError(AuthorizationException ex) {
        ex.printStackTrace();
        MessageResult result = MessageResult.error(5000, "unauthorized");
        return result;
    }

    @ResponseBody
    @ExceptionHandler({AuthenticationException.class,UnauthenticatedException.class})
    public MessageResult handleAuthenticationError(AuthorizationException ex) {
        ex.printStackTrace();
        MessageResult result = MessageResult.error(4000, "please login");
        return result;
    }
}
