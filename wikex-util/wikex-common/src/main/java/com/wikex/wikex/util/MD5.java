package com.wikex.wikex.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.security.MessageDigest;

public class MD5 {

    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D",
            "E", "F" };

    public static String md5(String text) throws Exception {

        String encode = DigestUtils.md5Hex(text);
        return encode;
    }

    public static String md5(String text, String key) throws Exception {

        String encode = DigestUtils.md5Hex(text + key);
        return encode;
    }

    public static boolean verify(String text, String key, String md5) throws Exception {

        String md5Text = md5(text, key);
        return md5Text.equalsIgnoreCase(md5);
    }

    public static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static byte[] md5Digest(byte[] src) throws Exception {
        MessageDigest alg = MessageDigest.getInstance("MD5");

        return alg.digest(src);
    }

    public static String md5Digest(String src) throws Exception {
        return byteArrayToHexString(md5Digest(src.getBytes("UTF-8")));
    }
}
