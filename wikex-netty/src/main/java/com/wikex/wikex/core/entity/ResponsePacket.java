
package com.wikex.wikex.core.entity;

/**
 * <p>Title: ResponsePacket</p>
 * <p>Description: </p>
 * 
 * @author MrGao
 * @date 
 */
public class ResponsePacket extends Packet {
	protected final static int HEADER_LENGTH = MIN_LENGTH + 4;
    private int code;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public int getHeaderLength() {
        return HEADER_LENGTH;
    }
}
