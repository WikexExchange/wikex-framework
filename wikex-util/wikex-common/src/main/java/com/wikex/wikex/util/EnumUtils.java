package com.wikex.wikex.util;

import com.wikex.wikex.constant.BooleanEnum;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class EnumUtils {


    public static <T> T getByCode(int code, Class<T> enumType){
        T[] enumConstants = enumType.getEnumConstants();
        for (T enumConstant : enumConstants) {
            Method getCode = null;
            try {
                getCode = enumType.getDeclaredMethod("getCode");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            Object invoke = null;
            try {
                invoke = getCode.invoke(enumConstant);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            if (Objects.equals(code, invoke)) {
                return enumConstant;
            }
        }
        return null;
    }


    public static <T> T getByName(String name,Class<T> enumType) {
        T[] enumConstants = enumType.getEnumConstants();
        for (T enumConstant : enumConstants) {
            if (Objects.equals(name, enumConstant.toString())) {
                return enumConstant;
            }
        }
        return null;
    }


    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        BooleanEnum is = EnumUtils.getByCode(1,BooleanEnum.class);
        System.out.println(is.getCode());
    }
}
