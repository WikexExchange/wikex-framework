/**
 * Copyright (c) 2016-2017  All Rights Reserved.
 * 
 * <p>FileName: HawkServerRealm.java</p>
 * 
 * Description: 
 * @author MrGao
 * @date 2019-07-24
 * @version 1.0
 * History:
 * v1.0.0, , 2019-07-24, Create
 */
package com.wikex.wikex.netty.shiro.realm;

import com.wikex.wikex.core.entity.CustomerMsg;
import com.wikex.wikex.netty.shiro.util.PasswordUtil;
import com.wikex.wikex.service.LoginUserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.realm.Realm;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>Title: HawkServerRealm</p>
 * <p>Description: Realm implementation for authentication</p>
 * @author MrGao
 * @date 2019-07-24
 */
public class HawkServerRealm implements Realm {
    @Autowired
    private LoginUserService loginUserService;

    @Override  
    public String getName() {  
        return "HawkServerRealm";  
    }  

    @Override  
    public boolean supports(AuthenticationToken token) {  
        // Only support UsernamePasswordToken type tokens  
        return token instanceof UsernamePasswordToken;   
    }  

    @SuppressWarnings("rawtypes")
    @Override  
    public AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {  
        String loginNo = (String) token.getPrincipal();  // Get username  
        String password = new String((char[]) token.getCredentials()); // Get password  
        
        CustomerMsg dbUser = loginUserService.findUserByLoginNo(loginNo);
        if (dbUser == null) {
            throw new UnknownAccountException(); // Username not found  
        } 
        
        String dbPwd = dbUser.getPassword();
        String salt = dbUser.getSalt();
        // Password is hashed by combining user ID + salt  
        String digestPwd = PasswordUtil.digestEncodedPassword(password, dbUser.getId() + salt);
        
        if (!dbPwd.equals(digestPwd)) {
            throw new IncorrectCredentialsException(); // Password incorrect  
        }  
        
        // If authentication succeeds, return AuthenticationInfo implementation  
        return new SimpleAuthenticationInfo(loginNo, password, getName());  
    }

    public static void main(String[] args){
//	    System.out.println("2ad18fc87f55c00ba273176a1349633453228dca55a8a9440b9f233a1b26cdd6bff6113206ec2ca2a1541a864e88167e404ff64eee40310c6eef5a420feb9308");
//	    System.out.println(PasswordUtil.digestEncodedPassword("d2f7575c5ea7c237725037a267c560f1", "9922286892116869133424271992021244351525401293731"));
    }
}
