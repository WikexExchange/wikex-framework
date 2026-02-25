package com.wikex.wikex.netty.codec;

import io.netty.channel.Channel;

/**
 * <p>Title: DefaultCodec</p>
 * <p>Description: </p>
 * Default encryption and decryption implementation
 * <ol>
 *     <li>Decoding without decryption</li>
 *     <li>Encoding without encryption</li>
 * </ol>
 * @author MrGao
 * @date 2019-06-26
 */
public class DefaultCodec implements Codec {

    @Override
    public byte[] decrypt(Channel channel, byte[] body) {
        // No decryption
        return body;
    }

    @Override
    public byte[] encrypt(Channel channel, byte[] body) {
        // No encryption
        return body;
    }
}
