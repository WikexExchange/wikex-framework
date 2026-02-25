/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkServerEncode.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-06-26
 * @version 1.0
 * History:
 * v1.0.0, 2019-06-26, Create
 */
package com.wikex.wikex.netty.codec;

import com.wikex.wikex.core.entity.ResponsePacket;
import com.wikex.wikex.core.exception.NettyException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Title: HawkServerEncoder
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * @author MrGao
 * @date 2019-06-26
 */
public class HawkServerEncoder extends MessageToByteEncoder<ResponsePacket> {
    private final static Logger LOGGER = LoggerFactory.getLogger(HawkServerEncoder.class);
    private Codec codec;

    public HawkServerEncoder() {
        this(new DefaultCodec());
    }

    public HawkServerEncoder(Codec codec) {
        this.codec = codec;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ResponsePacket packet, ByteBuf out) throws NettyException {
        LOGGER.debug("Original packet length: {}", packet.getLength());

        // Encrypt body
        byte[] body = codec.encrypt(ctx.channel(), packet.getBody());
        packet.setBody(body);

        LOGGER.debug("Packet length after encryption: {}", packet.getLength());

        // Write packet length
        out.writeInt(packet.getLength());
        // Write sequence ID
        out.writeLong(packet.getSequenceId());
        // Write command code
        out.writeShort(packet.getCmd());
        // Write response code
        out.writeInt(packet.getCode());
        // Write request ID
        out.writeInt(packet.getRequestId());
        // Write body
        if (body != null) {
            out.writeBytes(body);
        }
    }
}
