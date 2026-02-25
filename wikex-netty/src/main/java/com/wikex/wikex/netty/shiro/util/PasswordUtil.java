/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: PasswordUtil.java</p>
 * 
 * Description: Utility class for password hashing using Apache Shiro
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.shiro.util;

import org.apache.shiro.crypto.hash.ConfigurableHashService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashRequest;

/**
 * <p>Title: PasswordUtil</p>
 * <p>Description: Provides password hashing using SHA-512 via Apache Shiro</p>
 * @author MrGao
 * @date 2019-07-24
 */
public class PasswordUtil {
    // Default hashing algorithm
    public static final String DEFAULT_ALGORITHM = "SHA-512";

    /**
     * Hashes the given password with the specified salt using Apache Shiro hashing service.
     * @param password The raw password to hash
     * @param salt The salt used to strengthen the hash
     * @return Hex-encoded hashed password string
     */
    public static String digestEncodedPassword(final String password, String salt) {
        final ConfigurableHashService hashService = new DefaultHashService();
        hashService.setHashAlgorithmName(DEFAULT_ALGORITHM);
        hashService.setHashIterations(0);  // No additional hash iterations
        
        final HashRequest request = new HashRequest.Builder()
                .setSalt(salt)
                .setSource(password)
                .build();

        return hashService.computeHash(request).toHex();
    }
}
