//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.wikex.wikex.util;

import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Convert {
    public Convert() {
    }

    public static int strToInt(String str, int defaultValue) {
        try {
            defaultValue = Integer.parseInt(str);
        } catch (Exception var3) {
        }

        return defaultValue;
    }

    public static long strToLong(String str, long defaultValue) {
        long l = defaultValue;

        try {
            l = Long.parseLong(str);
        } catch (Exception var6) {
        }

        return l;
    }

    public static float strToFloat(String str, float defaultValue) {
        try {
            defaultValue = Float.parseFloat(str);
        } catch (Exception var3) {
        }

        return defaultValue;
    }

    public static double strToDouble(String str, double defaultValue) {
        double d = defaultValue;

        try {
            d = Double.parseDouble(str);
        } catch (Exception var6) {
        }

        return d;
    }

    public static boolean strToBoolean(String str, boolean defaultValue) {
        try {
            defaultValue = Boolean.parseBoolean(str);
        } catch (Exception var3) {
        }

        return defaultValue;
    }

    public static Date strToDate(String str, Date defaultValue) {
        return strToDate(str, "yyyy-MM-dd HH:mm:ss", defaultValue);
    }

    public static Date strToDate(String str, String formatStr, Date defaultValue) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);

        try {
            defaultValue = format.parse(str);
        } catch (Exception var5) {
        }

        return defaultValue;
    }

    public static String dateToStr(Date date, String defaultValue) {
        return dateToStr(date, "yyyy-MM-dd HH:mm:ss", defaultValue);
    }

    public static String dateToStr(Date date, String formatStr, String defaultValue) {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);

        try {
            defaultValue = format.format(date);
        } catch (Exception var5) {
        }

        return defaultValue;
    }

    public static String strToStr(String str, String defaultValue) {
        if (str != null && !str.isEmpty()) {
            defaultValue = str;
        }

        return defaultValue;
    }

    public  static String dotedCharacters(String str, Integer l) {
        if (!StringUtils.hasText(str)) {
            return str;
        }
        Integer len = str.length();
        if (len < l * 2)
            return str;

        if (!ValidateUtil.isEmail(str)) {
            return str.substring(0, l) + "..." + str.substring(len - l);
        }

        String[] parts = str.split("@");
        String user = parts[0];
        String domain = parts[1];

        if (user.length() <= l) {
            return str;
        }

        String prefix = user.substring(0, l);
        return prefix + "...@" + domain;
    }

    public static java.sql.Date dateToSqlDate(Date date) {
        return new java.sql.Date(date.getTime());
    }

    public static Date sqlDateToDate(java.sql.Date date) {
        return new Date(date.getTime());
    }

    public static Timestamp dateToSqlTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    public static Date qlTimestampToDate(Timestamp date) {
        return new Date(date.getTime());
    }

    public static int strtoAsc(String st) {
        return st.getBytes()[0];
    }

    public static char intToChar(int backnum) {
        return (char)backnum;
    }

    public static  <T> List<List<T>> chunkArrayList(List<T> list, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            int end = Math.min(list.size(), i + size);
            chunks.add(list.subList(i, end));
        }
        return chunks;
    }
}
