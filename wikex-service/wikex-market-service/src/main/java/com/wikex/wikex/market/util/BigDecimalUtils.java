package com.wikex.wikex.market.util;

import java.math.BigDecimal;


public class BigDecimalUtils {

    /**
     * Default division precision
     */
    private static final int DEFAULT_DIV_SCALE = 8;

    /**
     * Initialize using BigDecimal string constructor.
     *
     * @param v double value
     * @return BigDecimal object
     */
    private static BigDecimal createBigDecimal(double v) {
        return new BigDecimal(Double.toString(v));
    }

    /**
     * Provide precise addition.
     *
     * @param v1 augend
     * @param v2 addend
     * @return sum of two parameters
     */
    public static BigDecimal add(BigDecimal v1, BigDecimal v2) {
        return v1.add(v2);
    }

    /**
     * Provide precise addition.
     *
     * @param v1 augend
     * @param v2 addend
     * @return sum of two parameters
     */
    public static BigDecimal add(double v1, double v2) {
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        return b1.add(b2);
    }

    /**
     * Provide precise addition.
     *
     * @param v1 augend
     * @param v2 addend
     * @return sum of two parameters
     */
    public static BigDecimal add(BigDecimal v1, double v2) {
        BigDecimal b2 = createBigDecimal(v2);
        return v1.add(b2);
    }

    /**
     * Provide precise subtraction.
     *
     * @param v1 minuend
     * @param v2 subtrahend
     * @return difference of two parameters
     */
    public static BigDecimal sub(double v1, double v2) {
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        return b1.subtract(b2);
    }

    /**
     * Provide precise subtraction.
     *
     * @param v1 minuend
     * @param v2 subtrahend
     * @return difference of two parameters
     */
    public static BigDecimal sub(BigDecimal v1, double v2) {
        BigDecimal b2 = createBigDecimal(v2);
        return v1.subtract(b2);
    }

    /**
     * Provide precise subtraction.
     *
     * @param v1 minuend
     * @param v2 subtrahend
     * @return difference of two parameters
     */
    public static BigDecimal sub(BigDecimal v1, BigDecimal v2) {
        return v1.subtract(v2);
    }

    /**
     * Provide precise rounding for decimals.
     *
     * @param v     number to be rounded
     * @param scale number of digits to keep after decimal point
     * @return rounded result
     */
    public static BigDecimal round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = createBigDecimal(v);
        return b.divide(BigDecimal.ONE, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Provide precise rounding for decimals.
     *
     * @param v     number to be rounded
     * @param scale number of digits to keep after decimal point
     * @return rounded result
     */
    public static BigDecimal round(BigDecimal v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        return v.divide(BigDecimal.ONE, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Provide precise multiplication.
     *
     * @param v1 multiplicand
     * @param v2 multiplier
     * @return product of two parameters
     */
    public static BigDecimal mul(double v1, double v2) {
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        return b1.multiply(b2);
    }

    /**
     * Provide precise multiplication.
     *
     * @param v1 multiplicand
     * @param v2 multiplier
     * @return product of two parameters
     */
    public static BigDecimal mul(BigDecimal v1, double v2) {
        BigDecimal b2 = createBigDecimal(v2);
        return v1.multiply(b2);
    }

    /**
     * Provide precise multiplication.
     *
     * @param v1 multiplicand
     * @param v2 multiplier
     * @return product of two parameters
     */
    public static BigDecimal mul(BigDecimal v1, BigDecimal v2) {
        return v1.multiply(v2);
    }

    public static BigDecimal mulDown(BigDecimal v1, BigDecimal v2, int x) {
        return v1.multiply(v2).setScale(x, BigDecimal.ROUND_DOWN);
    }

    /**
     * Provide relatively precise multiplication, rounding to 8 decimal places.
     *
     * @param v1 multiplicand
     * @param v2 multiplier
     * @return product of two parameters
     */
    public static BigDecimal mulRound(BigDecimal v1, BigDecimal v2) {
        return mulRound(v1, v2, DEFAULT_DIV_SCALE);
    }

    /**
     * Provide relatively precise multiplication, rounding to v3 decimal places.
     *
     * @param v1 multiplicand
     * @param v2 multiplier
     * @param v3 number of digits to keep
     * @return product of two parameters
     */
    public static BigDecimal mulRound(BigDecimal v1, BigDecimal v2, int v3) {
        return round(v1.multiply(v2), v3);
    }

    /**
     * Provide (relatively) precise division.  
     * When division is not exact, the scale parameter specifies precision, 
     * and the result is rounded.
     *
     * @param v1    dividend
     * @param v2    divisor
     * @param scale number of digits after decimal point
     * @return quotient of two parameters
     */
    public static BigDecimal div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b1 = createBigDecimal(v1);
        BigDecimal b2 = createBigDecimal(v2);
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Provide (relatively) precise division.  
     * When division is not exact, the scale parameter specifies precision, 
     * and the result is rounded.
     *
     * @param v1    dividend
     * @param v2    divisor
     * @param scale number of digits after decimal point
     * @return quotient of two parameters
     */
    public static BigDecimal div(BigDecimal v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b2 = createBigDecimal(v2);
        return v1.divide(b2, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Provide (relatively) precise division.  
     * When division is not exact, the scale parameter specifies precision, 
     * and the result is rounded.
     *
     * @param v1    dividend
     * @param v2    divisor
     * @param scale number of digits after decimal point
     * @return quotient of two parameters
     */
    public static BigDecimal div(BigDecimal v1, BigDecimal v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        return v1.divide(v2, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Provide (relatively) precise division.  
     * When division is not exact, defaults to 8 decimal places, result is rounded.
     *
     * @param v1 dividend
     * @param v2 divisor
     * @return quotient of two parameters
     */
    public static BigDecimal div(BigDecimal v1, BigDecimal v2) {
        return v1.divide(v2, DEFAULT_DIV_SCALE, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal divDown(BigDecimal v1, BigDecimal v2) {
        return v1.divide(v2, DEFAULT_DIV_SCALE, BigDecimal.ROUND_DOWN);
    }

    /**
     * Get interest rate.
     *
     * @param v1 value
     * @return rate
     */
    public static BigDecimal getRate(BigDecimal v1) {
        BigDecimal hundred = new BigDecimal("100");
        return div(v1, hundred);
    }

    /**
     * Get multiplier.
     *
     * @param v1 value
     * @return result
     */
    public static BigDecimal rate(BigDecimal v1) {
        return add(getRate(v1), BigDecimal.ONE);
    }

    /**
     * Compare two values.  
     * If v1 >= v2 return true, otherwise return false.
     *
     * @param v1 value 1
     * @param v2 value 2
     * @return comparison result
     */
    public static boolean compare(BigDecimal v1, BigDecimal v2) {
        return v1.compareTo(v2) >= 0;
    }

    /**
     * Compare (v1+v2) with v3.  
     * If (v1+v2) >= v3 return true, otherwise return false.
     *
     * @param v1 value 1
     * @param v2 value 2
     * @param v3 value 3
     * @return comparison result
     */
    public static boolean compare(BigDecimal v1, BigDecimal v2, BigDecimal v3) {
        return compare(add(v1, v2), v3);
    }

    /**
     * Determine whether two values are equal.
     *
     * @param v1 value 1
     * @param v2 value 2
     * @return true if equal, otherwise false
     */
    public static boolean isEqual(BigDecimal v1, BigDecimal v2) {
        return v1.compareTo(v2) == 0;
    }
}
