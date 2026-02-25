/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: ObsoletedType.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date July 18, 2019
 * @version 1.0
 * History:
 * v1.0.0, July 18, 2019, Create
 */
package com.wikex.wikex.core.annotation;

/**
 * <p>Title: ObsoletedType</p>
 * <p>Description: </p>
 * Indicates whether a service method has expired.
 * Expired service methods can no longer be accessed.
 * @author MrGao
 * @date July 18, 2019
 */
public enum ObsoletedType {
     YES, NO;

    public static boolean isObsoleted(ObsoletedType type) {
        if (YES == type) {
            return true;
        } else {
            return false;
        }
    }
}
