package com.wikex.wikex.util;
import java.util.Base64;

public class Base64Util {

    
    public static byte[] decode(String encodedText){
        final Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(encodedText);
    }

    
    public static String encode(byte[] data){
        final Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(data);
    }

    
    public static byte[] decodeURL(String encodedText){
        final Base64.Decoder decoder = Base64.getUrlDecoder();
        return decoder.decode(encodedText);
    }

    
    public static String encodeURL(byte[] data){
        final Base64.Encoder encoder = Base64.getUrlEncoder();
        return encoder.encodeToString(data);
    }

    public static void main(String[] args) throws Exception {
        String str = "Today's meal was really delicious! I'll come again tomorrow!";
        String encode = encode(str.getBytes("UTF-8"));
        byte[] decode = decode(encode);
        System.out.println(new String(decode, "UTF-8"));
        System.out.println(encode);

        String s = encodeURL(str.getBytes("UTF-8"));
        byte[] bytes = decodeURL(s);
        System.out.println(new String(bytes, "UTF-8"));
        System.out.println(s);

    }
}