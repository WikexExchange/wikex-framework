/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkMethod.java</p>
 * 
 * Description: 
 * author MrGao
 * @date 2019-07-18
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-18, Create
 */
package com.wikex.wikex.core.annotation;

import java.lang.annotation.*;

/**
 * <p>Title: HawkMethod</p>
 * <p>Description: </p>
 * Use this annotation to mark service methods.
 * 
 * author MrGao
 * @date 2019-07-18
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HawkMethod {
    /**
     * Command ID
     * @return
     */
    int cmd();

    /**
     * Command version number
     * @return
     */
    byte version() default 1;

    /**
     * Whether the service method is obsolete. Defaults to not obsolete.
     */
    ObsoletedType obsoleted() default ObsoletedType.NO;
}
