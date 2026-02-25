package com.wikex.wikex.rpc.util;

import java.security.MessageDigest;

public class Md5 {
	private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D",
			"E", "F" };

	public static String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest(s.getBytes("UTF-8"));
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Convert a byte array to a hexadecimal string
	 * 
	 * @param b byte array
	 * @return hexadecimal string
	 */
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

	/**
	 * MD5 digest computation (byte[]).
	 * 
	 * @param src byte[]
	 * @throws Exception
	 * @return byte[] 16-byte digest
	 */
	public static byte[] md5Digest(byte[] src) throws Exception {
		MessageDigest alg = MessageDigest.getInstance("MD5"); // MD5 is a 16-byte message digest
		return alg.digest(src);
	}

	/**
	 * MD5 digest computation (String).
	 * 
	 * @param src String
	 * @throws Exception
	 * @return String
	 */
	public static String md5Digest(String src) throws Exception {
		return byteArrayToHexString(md5Digest(src.getBytes("UTF-8")));
	}

	/** Test encryption */
	/*
	 * public static void main(String[] args) {
	 * try {
	 * // Example of plain text data
	 * // String desStr =
	 * "MERCHANTID=123456789&ORDERSEQ=20060314000001&ORDERDATE=20060314&ORDERAMOUNT=10000";
	 * // System.out.println("Original string desStr == " + desStr);
	 * // // Generate MAC
	 * // String MAC = md5Digest(desStr);
	 * // System.out.println("MAC == " + MAC);
	 * //
	 * // // Use key value to generate SIGN
	 * // String keyStr = "304F3A86606C585E"; // Use fixed key
	 * // // Example of plain text data
	 * // desStr =
	 * "UPTRANSEQ=20090327190924&MERCHANTID=3400000001&ORDERID=55120090327843921320&PAYMENT=1&RETNCODE=0000&RETNINFO=0000&PAYDATE=20090327";
	 * // // Combine key value and plain text into a string to be signed
	 * // desStr = desStr + "&KEY=" + keyStr;
	 * // System.out.println("Original string desStr == " + desStr);
	 * // // Generate SIGN
	 * // String SIGN = md5Digest(desStr);
	 * // System.out.println("SIGN == " + SIGN);
	 * String desStr ="LiLiMengXiangTongXing2014";
	 * String desStr_after = md5Digest(desStr);
	 * System.out.println("After encryption: desStr_after:" + desStr_after +
	 * " length:" + desStr_after.length());
	 * if(desStr_after.equalsIgnoreCase("96e79218965eb72c92a549dd5a330112")){
	 * System.out.println("Match");
	 * }
	 * } catch (Exception ex) {
	 * ex.printStackTrace();
	 * }
	 * }
	 */
}
