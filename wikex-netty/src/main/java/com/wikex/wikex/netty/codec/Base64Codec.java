package com.wikex.wikex.netty.codec;

import io.netty.channel.Channel;
import java.util.Base64;

/**
 * <p>Title: Base64Codec</p>
 * <p>Description: </p>
 * Base64 encryption and decryption
 * @author MrGao
 * @date 2019-06-26
 */
public class Base64Codec implements Codec {

    @Override
    public byte[] decrypt(Channel channel, byte[] body) {
        return Base64.getDecoder().decode(body);
    }

    @Override
    public byte[] encrypt(Channel channel, byte[] body) {
        return Base64.getEncoder().encode(body);
    }
}
