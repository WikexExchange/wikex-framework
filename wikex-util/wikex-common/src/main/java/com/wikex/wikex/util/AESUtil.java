package com.wikex.wikex.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;

public class AESUtil {


    
    public static byte[] encryptAndDecrypt(byte[] buffer,String appsecret,Integer mode) throws Exception{
        
        Security.addProvider(new BouncyCastleProvider());

        
        SecretKeySpec secretKeySpec = new SecretKeySpec(appsecret.getBytes("UTF-8"),"AES");

        
        
        
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding","BC");
        
        cipher.init(mode,secretKeySpec);
        
        return cipher.doFinal(buffer);
    }

    
    public static void main_backup(String[] args) throws Exception{
        String txt = "SpringCloud Alibaba";
        String appsecret="aaaaaaaaaaaaaaaa";
        Integer mode=1;

        
        byte[] bytes = encryptAndDecrypt(txt.getBytes("UTF-8"), appsecret, mode);
        String encode = Base64Util.encode(bytes);
        System.out.println(encode);

        
        byte[] decode = encryptAndDecrypt(Base64Util.decode(encode), appsecret, 2);
        System.out.println(new String(decode, "UTF-8"));

    }

    
    public static void main(String[] args) throws Exception{
        String txt = "SpringCloud Alibaba";
        String appsecret="aaaaaaaaaaaaaaaa";
        appsecret = MD5.md5(appsecret);
        System.out.println(appsecret);
        Integer mode=1;

        
        byte[] bytes = encryptAndDecrypt(txt.getBytes("UTF-8"), appsecret, mode);
        String encode = Base64Util.encode(bytes);
        System.out.println(encode);

        
        byte[] decode = encryptAndDecrypt(Base64Util.decode(encode), appsecret, 2);
        System.out.println(new String(decode, "UTF-8"));

    }
}
