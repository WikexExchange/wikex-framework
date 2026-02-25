package com.wikex.wikex.core.entity;

/**
 * <p>Title: RequestPacket</p>
 * <p>Description: </p>
 * Request data packet
 * @author MrGao
 * @date 2019-07-19
 */
public class RequestPacket  extends Packet{
	protected final static int HEADER_LENGTH = MIN_LENGTH + 8;
    /**
     * Command version number, 4 bytes
     */
    private int version;
    /**
     * Terminal type, 4 bytes
     */
    private String terminalType;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
    public String getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(String terminalType) {
		this.terminalType = terminalType;
	}

	@Override
    public int getHeaderLength() {
        return HEADER_LENGTH;
    }
}
