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

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: LoginUserDao</p>
 * <p>Description: </p>
 * Default service for Netty login, username can be arbitrary, password is "admin"
 * @author MrGao
 * @date 2019-07-24
 */
@SuppressWarnings("rawtypes")
public class DefaultLoginUserService implements LoginUserService {


	@Override
	public CustomerMsg findUserByLoginNo(String loginNo) {
		Map<String,String> result = new HashMap<>();
		CustomerMsg customerMsg = new CustomerMsg();
		customerMsg.setPassword("0c2eea5ef044ce91e0bf4191593c7c1e08126b428c29594de7df5cbdb74b4c90931ee1193b75e50bbc3f8e539605e75a3f2ce88a789d1bfbabf45a1ed2bce849");
		customerMsg.setSalt("123456");
		return customerMsg;
	}


	@Override
	public Integer updPassword(String accountNo, String password) {
		return 1;
	}

}
