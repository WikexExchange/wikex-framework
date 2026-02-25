/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkServerDecode.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-06-26
 * @version 1.0
 * History:
 * v1.0.0, 2019-06-26, Create
 */
package com.wikex.wikex.netty.codec;

import com.wikex.wikex.core.entity.RequestPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <p>
 * Title: HawkServerDecoder
 * </p>
 * <p>
 * Description:
 * </p>
 * Netty decoder for server-side processing of request packets.
 * Reads binary data from the channel, decodes it into a RequestPacket object,
 * and optionally decrypts the body using the specified Codec.
 * 
 * The decoding order:
 * <ol>
 * <li>Read packet length</li>
 * <li>Read sequence ID</li>
 * <li>Read command code</li>
 * <li>Read version</li>
 * <li>Read terminal type</li>
 * <li>Read request ID</li>
 * <li>Read body bytes (and optionally decrypt)</li>
 * </ol>
 * Adds the decoded packet to the output list for the next handler in the
 * pipeline.
 * 
 * @author MrGao
 * @date 2019-06-26
 */
public class HawkServerDecoder extends ByteToMessageDecoder {
	private final static Logger LOGGER = LoggerFactory.getLogger(HawkServerDecoder.class);
	private Codec codec;

	public HawkServerDecoder() {
		this(new DefaultCodec());
	}

	public HawkServerDecoder(Codec codec) {
		this.codec = codec;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) {
		RequestPacket packet = new RequestPacket();
		if (byteBuf == null || !ctx.channel().isActive()) {
			return;
		}
		int packetLen = byteBuf.readInt();
		LOGGER.debug("Original packet length: {}", packetLen);

		// Set sequence ID
		packet.setSequenceId(byteBuf.readLong());
		// Set command code
		packet.setCmd(byteBuf.readShort());
		// Set command version
		packet.setVersion(byteBuf.readInt());

		byte[] termByte = new byte[4];
		byteBuf.readBytes(termByte);
		packet.setTerminalType(new String(termByte));

		packet.setRequestId(byteBuf.readInt());

		byte[] tytes = new byte[byteBuf.readableBytes()];
		byteBuf.readBytes(tytes);

		// Decrypt (currently disabled)
		// packet.setBody(codec.decrypt(ctx.channel(), tytes));
		packet.setBody(tytes);

		// Packet length after decryption
		packetLen = packet.getLength();
		LOGGER.debug("Packet length after decryption: {}", packetLen);

		// Reset packet length
		packet.setLength(packetLen);

		list.add(packet);
	}
}
