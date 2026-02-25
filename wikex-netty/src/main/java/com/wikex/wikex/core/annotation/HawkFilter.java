/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkFilter.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-18
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-18, Create
 */
package com.wikex.wikex.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * <p>Title: HawkFilter</p>
 * <p>Description: </p>
 * Filter annotation
 * 
 * @author MrGao
 * @date 2019-07-18
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface HawkFilter {
    /**
     * Execution order. The smaller the value, the earlier the before method is executed, 
     * and the later the after method is executed.
     * @return
     */
    int order() default 0;

    /**
     * List of intercepted command IDs.
     * @return
     */
    int[] cmds() default {};

    /**
     * List of ignored command IDs (commands that will not be intercepted).
     * @return
     */
    int[] ignoreCmds() default {};
}
