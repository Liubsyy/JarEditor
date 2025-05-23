package com.liubs.jareditor.util;

import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * @author Liubsyy
 * @date 2025/5/23
 */
public class JsonUtil {
    private static Gson gson = new Gson();

    public static String toJson(Object o) {
        return gson.toJson(o);
    }


    public static final <V> V parse(String json, Class<V> type) {
        return gson.fromJson(json, type);
    }

    public static final <V> V parse(String json, Type type) {
        return gson.fromJson(json, type);
    }

}
