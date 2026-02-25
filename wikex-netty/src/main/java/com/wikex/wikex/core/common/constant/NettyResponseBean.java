/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 *
 * <p>FileName: NettyResponseBean.java</p>
 *
 * Description:
 * @author MrGao
 * @date July 20, 2019
 * @version 1.0
 * History:
 * v1.0.0, July 20, 2019, Create
 */
package com.wikex.wikex.core.common.constant;

/**
 * <p>
 * Title: NettyResponseBean
 * </p>
 * <p>
 * Description:
 * </p>
 * 
 * @author MrGao
 * @date July 20, 2019
 */
public class NettyResponseBean {
	private int responseCode;
	private String responseMessage;

	/**
	 * <p>
	 * Title:
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 *
	 * @param responseCode
	 * @param responseMessage
	 */
	public NettyResponseBean(int responseCode, String responseMessage) {
		super();
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	/**
	 * <p>
	 * Title: getResponseString
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * Returns the error code and error message, separated by "~"
	 * 
	 * @return
	 */
	public String getResponseString() {
		return responseCode + "~" + responseMessage;
	}
}
