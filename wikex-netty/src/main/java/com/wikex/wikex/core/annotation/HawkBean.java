/*
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkBean.java</p>
 * 
 * Description: 
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-18, Create
 */
package com.wikex.wikex.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * <p>Title: HawkBean</p>
 * <p>Description: </p>
 * Mark this class in a service class to determine the group and related information 
 * to which the service method belongs.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface HawkBean {
    /**
     * Command version number
     * @return
     */
    byte version() default 0;
}
