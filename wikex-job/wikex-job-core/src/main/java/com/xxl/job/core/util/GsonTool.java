package com.xxl.job.core.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Gson utility class for JSON serialization and deserialization
 * 
 * @author william 2020-04-11 20:56:31
 */
public class GsonTool {

    private static Gson gson = null;
    static {
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    /**
     * Convert Object to JSON string
     *
     * @param src source object
     * @return JSON string
     */
    public static String toJson(Object src) {
        return gson.toJson(src);
    }

    /**
     * Convert JSON string to Object of specific class
     *
     * @param json JSON string
     * @param classOfT target class
     * @return Object of classOfT
     */
    public static <T> T fromJson(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    /**
     * Convert JSON string to Object of parameterized type rawClass<classOfT>
     *
     * @param json JSON string
     * @param classOfT raw class
     * @param argClassOfT parameter class
     * @return Object of parameterized type
     */
    public static <T> T fromJson(String json, Class<T> classOfT, Class argClassOfT) {
        Type type = new ParameterizedType4ReturnT(classOfT, new Class[]{argClassOfT});
        return gson.fromJson(json, type);
    }

    public static class ParameterizedType4ReturnT implements ParameterizedType {
        private final Class raw;
        private final Type[] args;

        public ParameterizedType4ReturnT(Class raw, Type[] args) {
            this.raw = raw;
            this.args = args != null ? args : new Type[0];
        }

        @Override
        public Type[] getActualTypeArguments() {
            return args;
        }

        @Override
        public Type getRawType() {
            return raw;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }

    /**
     * Convert JSON string to List of objects of specific class
     *
     * @param json JSON string
     * @param classOfT class of list elements
     * @return List of objects of classOfT
     */
    public static <T> List<T> fromJsonList(String json, Class<T> classOfT) {
        return gson.fromJson(
                json,
                new TypeToken<List<T>>() {
                }.getType()
        );
    }

}
