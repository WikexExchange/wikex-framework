package com.wikex.wikex.rpc.component;

import com.wikex.wikex.rpc.entity.TokenInputData;
import org.tron.common.utils.ByteArray;
import org.tron.utils.TronUtils;
import org.web3j.abi.TypeDecoder;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: little liu
 * @Date: 2020/09/03/16:03
 * @Description: Utility class for data transformation and encoding/decoding operations
 */
public class TransformUtil {

    /**
     * Pad zeros to the left if the length is not enough.
     *
     * @param str       Original string
     * @param strLength Expected total length
     * @return Zero-padded string
     */
    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        if (strLen < strLength) {
            while (strLen < strLength) {
                StringBuffer sb = new StringBuffer();
                sb.append("0").append(str); // Left pad with zero
                // sb.append(str).append("0"); // Right pad with zero
                str = sb.toString();
                strLen = str.length();
            }
        }
        return str;
    }

    public static String delZeroForNum(String str) {
        return str.replaceAll("^(0+)", "");
    }

    public static String getSeqNumByLong(Long l, int bitCount) {
        return String.format("%0" + bitCount + "d", l);
    }

    /**
     * Convert string to hexadecimal string.
     *
     * @param s Input string
     * @return Hexadecimal representation
     */
    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /**
     * Convert hexadecimal string to normal string.
     *
     * @param s Hexadecimal string
     * @return Decoded string
     */
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "gbk");
            new String();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    /**
     * Convert a hexadecimal string to a byte array.
     *
     * @param s Hexadecimal string
     * @return Byte array
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // Every two hex characters represent one byte
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }

    /**
     * Convert byte array to hexadecimal string.
     *
     * @param bArray Byte array
     * @return Hexadecimal string
     */
    public static final String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * Convert hexadecimal string to decimal integer by position weight calculation.
     */
    public static int hexToDecimal(String hex) {
        int outcome = 0;
        for (int i = 0; i < hex.length(); i++) {
            char hexChar = hex.charAt(i);
            outcome = outcome * 16 + charToDecimal(hexChar);
        }
        return outcome;
    }

    /**
     * Convert a hexadecimal character to its decimal value.
     */
    public static int charToDecimal(char c) {
        if (c >= 'A' && c <= 'F')
            return 10 + c - 'A';
        else
            return c - '0';
    }

    /**
     * Split a string into a list of strings of the specified length.
     *
     * @param inputString Original string
     * @param length      Chunk length
     */
    public static List<String> getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * Split a string into a list of strings of the specified length and list size.
     *
     * @param inputString Original string
     * @param length      Chunk length
     * @param size        Number of chunks
     */
    public static List<String> getStrList(String inputString, int length, int size) {
        List<String> list = new ArrayList<>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    /**
     * Substring with safe bounds. Returns null if start index exceeds length.
     */
    public static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }

    public static String encode58Check(String str) {
        str = TransformUtil.delZeroForNum(str);
        if (!str.startsWith("41")) {
            str = "41" + str;
        }
        byte[] bytes = ByteArray.fromHexString(str);
        return TronUtils.encode58Check(bytes);
    }

    public static TokenInputData getTokenInputData(String inputData) {
        Method refMethod;
        TokenInputData tokenInputData = null;
        if (inputData == null || inputData.length() < 10) {
            return null;
        }
        try {
            refMethod = TypeDecoder.class.getDeclaredMethod("decode", String.class, int.class, Class.class);
            refMethod.setAccessible(true);
            String method = inputData.substring(0, 8);
            String to = null;
            String amount = null;
            if (inputData.length() > 72) {
                to = inputData.substring(10, 72);
                amount = inputData.substring(72);
            }
            tokenInputData = new TokenInputData(method, to, amount);
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
            e.printStackTrace();
        }
        return tokenInputData;
    }

    /**
     * Format token amount from raw balance.
     *
     * @param balance Raw balance (BigInteger)
     * @param decimal Token decimal precision
     * @return Token amount as BigDecimal
     */
    public static BigDecimal formatTokenNum(BigInteger balance, int decimal) {
        BigDecimal num = BigDecimal.valueOf(balance.longValue()).divide(BigDecimal.TEN.pow(decimal), 8, BigDecimal.ROUND_DOWN);
        return num;
    }
}
