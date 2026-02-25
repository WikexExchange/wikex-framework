/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: Packet.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-02
 * @version 1.0
 * History:
 * v1.0.0, 2019-07-02, Create
 */
package com.wikex.wikex.core.entity;

/**
 * <p>
 * Title: Packet
 * </p>
 * <p>
 * Description:
 * </p>
 * Definition of the data packet for interaction between Netty and the client
 * <ul>
 * <li>Declaration of general packet information, including length, sequence ID,
 * command code, and content</li>
 * <li>Length: Defaults to the header length, reset to the sum of the header
 * length and body length when setBody is called</li>
 * <li>Sequence ID: For requests, this value is the unique ID of the client; for
 * responses, it is a uniquely generated code</li>
 * <li>Command code: The specific action required from the other party</li>
 * </ul>
 * 
 * @author MrGao
 * @date 2019-07-02
 */
public abstract class Packet {

    protected final static int MIN_LENGTH = 18;

    /**
     * Packet length: 4 bytes, the length of each frame's packet, to prevent TCP
     * packet sticking
     */
    private int length;

    /**
     * Unique sequence ID of the client, 8 bytes
     */
    private long sequenceId;

    /**
     * Client request ID, 4 bytes
     */
    private int requestId;

    /**
     * Command code, 2 bytes
     */
    private short cmd;

    /**
     * Specific packet content, encoded with Protocol Buffers
     */
    private byte[] body;

    public abstract int getHeaderLength();

    public int getLength() {
        if (length == 0) {
            return getHeaderLength();
        }
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public short getCmd() {
        return cmd;
    }

    public void setCmd(short cmd) {
        this.cmd = cmd;
    }

    public byte[] getBody() {
        return body;
    }
    
    public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}


	public void setBody(byte [] body) {
        this.body = body;
        if (this.body == null) {
            this.length = getHeaderLength();
        } else {
            this.length = getHeaderLength() + this.body.length;
        }
    }

    public long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(long sequenceId) {
        this.sequenceId = sequenceId;
    }
}
