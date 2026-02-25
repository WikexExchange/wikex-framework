package com.wikex.wikex.advice;

import com.wikex.wikex.exception.*;
import com.wikex.wikex.util.MessageResult;
import com.wikex.wikex.service.LocaleMessageSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
@Slf4j
public class ExceptionControllerAdvice {

    @Autowired
    private LocaleMessageSourceService msService;

    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public MessageResult myErrorHandler(IllegalArgumentException e, HttpServletResponse response) {
        // e.printStackTrace();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        log.error(">>>Intercept parameter exception>>", e);
        MessageResult result = MessageResult.error(e.getMessage());
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = ServletRequestBindingException.class)
    public MessageResult myErrorHandler(ServletRequestBindingException e, HttpServletResponse response) {
        // e.printStackTrace();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        log.error(">>>Intercept parameter binding exception>>", e);
        MessageResult result = MessageResult.error(3000, msService.getMessage("PARAMETER_BINDING_ERROR"));
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = InformationExpiredException.class)
    public MessageResult myErrorHandler(InformationExpiredException ex, HttpServletResponse response) {
        // ex.printStackTrace();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        log.error(">>>Intercept data expired exception>>", ex);
        MessageResult result = MessageResult.error(msService.getMessage("DATA_OUT_OF_DATE"));
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = AuthenticationException.class)
    public MessageResult myErrorHandler(AuthenticationException ex, HttpServletResponse response) {
        // ex.printStackTrace();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        log.error(">>>Authorization exception>>", ex);
        MessageResult result = MessageResult.error(ex.getMessage());
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = GeeTestException.class)
    public MessageResult myErrorHandler(GeeTestException ex, HttpServletResponse response) {
        // ex.printStackTrace();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        log.error(">>>Intercept GeeTest verification failed exception>>", ex);
        MessageResult result = MessageResult.error(ex.getMessage());
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = GAException.class)
    public MessageResult myErrorHandler(GAException ex, HttpServletResponse response) {
        // ex.printStackTrace();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        log.error(">>>Intercept GA verification failed exception>>", ex);
        MessageResult result = MessageResult.error(505, ex.getMessage());
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public MessageResult myErrorHandler(Exception ex, HttpServletResponse response) {
        // ex.printStackTrace();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        log.error(">>>Intercept exception>>", ex);
        MessageResult result = MessageResult.error(msService.getMessage("UNKNOWN_ERROR"));
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    public MessageResult httpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletResponse response) {
        // ex.printStackTrace();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        log.error(">>>Wrong request method exception>>", ex);
        String methods = "";

        String[] supportedMethods = ex.getSupportedMethods();
        for (String method : supportedMethods) {
            methods += method;
        }
        MessageResult result = MessageResult.error("Request method " + ex.getMethod() + "  not supported !" +
                " supported method : " + methods + "!");
        return result;
    }

    @ResponseBody
    @ExceptionHandler(value = WikexRuntimeException.class)
    public MessageResult WikexRuntimeExceptionHandler(WikexRuntimeException ex, HttpServletResponse response) {
        // ex.printStackTrace();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        log.error(">>>Intercept custom exception>>", ex);
        MessageResult result = MessageResult.error(ex.getMessage());
        return result;
    }

    public static void main(String[] args) {
//        String originalString = "600800,600802,600803,600804";
//        int targetIndex = originalString.indexOf("600803");
//
//        if (targetIndex != -1) {
//            System.out.println(originalString.substring(0, targetIndex).trim() + "600803");
//        }
    }
}
