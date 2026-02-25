package com.wikex.wikex.netty.codec;

import io.netty.channel.Channel;

/**
 * <p>Title: Codec</p>
 * <p>Description: </p>
 * Encryption and decryption interface
 * @author MrGao
 * @date 2019-06-26
 */
public interface Codec {

    /**
     * Decrypt the body part of the message
     * @param channel IO connection channel information
     * @param body Message body bytes
     * @return Decrypted byte array
     */
    byte[] decrypt(Channel channel, byte[] body);

    /**
     * Encrypt the body part of the response
     * @param channel IO connection channel information
     * @param body Response body bytes
     * @return Encrypted byte array
     */
    byte[] encrypt(Channel channel, byte[] body);
}
