package com.wikex.wikex.util;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;


public class AESUtils {

    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";

    
    private static final String SALTPRE="KaiYuan";

    
    public static String encrypt(String content, String password) throws Exception {
        Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
        byte[] byteContent = content.getBytes(DEFAULT_CHARSET);
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(password));
        byte[] result = cipher.doFinal(byteContent);
        return parseByte2HexStr(result);
    }

    
    public static String decrypt(String content, String password) throws Exception {
        
        Cipher cipher = Cipher.getInstance(DEFAULT_CIPHER_ALGORITHM);
        
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(password));
        
        byte[] result = cipher.doFinal(parseHexStr2Byte(content));
        return new String(result, DEFAULT_CHARSET);
    }

    
    private static SecretKeySpec getSecretKey(final String password) throws Exception {
        
        KeyGenerator kg = null;
        kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG" );
        secureRandom.setSeed(password.getBytes());
        kg.init(128, secureRandom);
        SecretKey secretKey = kg.generateKey();
        return new SecretKeySpec(secretKey.getEncoded(), KEY_ALGORITHM);
    }

    
    public static String parseByte2HexStr(byte buf[]) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    
    public static String customSaltContent(){
        return DigestUtils.md5Hex(SALTPRE).substring(8, 24);
    }
    public static void main(String[] args) throws Exception{


        String s = "29c3f66ad3e529c134c9aab616df557cf1b58229ab787ad94190b6512eda90b7";
        System.out.println("Plaintext before encryption: " + s);
        System.out.println("Ciphertext: " + AESUtils.encrypt(s, "wikex"));
        String s1 = "6EB7FDDFDA053C9B8661CB23D27946E99613FB1219B17E43C412BF9559CF55AADE50F8326A5E4B8D3A10F944C80EB9B7B4537EADD5568A9D3680E71035862665B5E0846B1B5FFAA540BC7E303756C086";

        System.out.println("Plaintext after decryption: " + AESUtils.decrypt(s1, "wikex"));

    }

}