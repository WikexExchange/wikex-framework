/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: LoginUserDao.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.service;


import com.wikex.wikex.core.entity.CustomerMsg;

/**
 * <p>Title: LoginUserDao</p>
 * <p>Description: </p>
 * @author MrGao
 * @date 2019-07-24
 */
@SuppressWarnings("rawtypes")
public interface LoginUserService {
	/**
	 * 
	 * <p>Title: findUserByLoginNo</p>
	 * <p>Description: </p>
	 * Query user information by username
	 * @param loginNo
	 * @return
	 */
	public CustomerMsg findUserByLoginNo(String loginNo);

	public Integer updPassword(String accountNo, String password);
}
