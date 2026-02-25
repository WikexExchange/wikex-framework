package com.wikex.wikex.util;

import java.math.BigDecimal;


public class BigDecimalUtils {

    
    private static final int DEFAULT_DIV_SCALE = 8;

    
    private static BigDecimal createBigDecimal(double v) {
        return new BigDecimal(Double.toString(v));
    }

    
    public static BigDecimal add(BigDecimal v1, BigDecimal v2) {
        v1=v1==null?BigDecimal.ZERO:v1;
        v2=v2==null?BigDecimal.ZERO:v2;
        return v1.add(v2);
    }

    
    public static BigDecimal add(double v1, double v2) {
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        return b1.add(b2);
    }

    
    public static BigDecimal add(BigDecimal v1, double v2) {
        BigDecimal b2 = createBigDecimal(v2);
        return v1.add(b2);
    }

    
    public static BigDecimal sub(double v1, double v2) {
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        return b1.subtract(b2);
    }

    
    public static BigDecimal sub(BigDecimal v1, double v2) {
        BigDecimal b2 = createBigDecimal(v2);
        return v1.subtract(b2);
    }

    
    public static BigDecimal sub(BigDecimal v1, BigDecimal v2) {
        return v1.subtract(v2);
    }

    
    public static BigDecimal round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = createBigDecimal(v);
        return b.divide(BigDecimal.ONE, scale, BigDecimal.ROUND_HALF_UP);
    }

    
    public static BigDecimal round(BigDecimal v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        return v.divide(BigDecimal.ONE, scale, BigDecimal.ROUND_HALF_UP);
    }

    
    public static BigDecimal mul(double v1, double v2) {
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        return b1.multiply(b2);
    }

    
    public static BigDecimal mul(BigDecimal v1, double v2) {
        BigDecimal b2 = createBigDecimal(v2);
        return v1.multiply(b2);
    }

    
    public static BigDecimal mul(BigDecimal v1, BigDecimal v2) {
        return v1.multiply(v2);
    }

    public static BigDecimal mulDown(BigDecimal v1, BigDecimal v2,int x) {
        return v1.multiply(v2).setScale(x,BigDecimal.ROUND_DOWN);
    }
    
    public static BigDecimal mulRound(BigDecimal v1, BigDecimal v2) {
        return mulRound(v1, v2, DEFAULT_DIV_SCALE);
    }

    
    public static BigDecimal mulRound(BigDecimal v1, BigDecimal v2, int v3) {
        return round(v1.multiply(v2), v3);
    }

    
    public static BigDecimal div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }

    
    public static BigDecimal div(BigDecimal v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b2 = createBigDecimal(v2);
        return v1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }

    
    public static BigDecimal div(BigDecimal v1, BigDecimal v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        return v1.divide(v2, scale, BigDecimal.ROUND_HALF_UP);
    }

    
    public static BigDecimal div(BigDecimal v1, BigDecimal v2) {
        return v1.divide(v2, DEFAULT_DIV_SCALE, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal divDown(BigDecimal v1, BigDecimal v2) {
        return v1.divide(v2, DEFAULT_DIV_SCALE, BigDecimal.ROUND_DOWN);
    }

    
    public static BigDecimal getRate(BigDecimal v1) {
        BigDecimal hundred = new BigDecimal("100");
        return div(v1, hundred);
    }

    
    public static BigDecimal rate(BigDecimal v1) {
        return add(getRate(v1), BigDecimal.ONE);
    }

    
    public static boolean compare(BigDecimal v1, BigDecimal v2) {
        return v1.compareTo(v2) >= 0 ? true : false;
    }

    
    public static boolean compare(BigDecimal v1, BigDecimal v2, BigDecimal v3) {
        return compare(add(v1, v2), v3);
    }

    
    public static boolean isEqual(BigDecimal v1, BigDecimal v2) {
        return v1.compareTo(v2) == 0 ? true : false;
    }
}
