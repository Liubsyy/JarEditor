package com.liubs.jareditor.util;

import java.util.UUID;

/**
 * @author Liubsyy
 * @date 2024/5/14
 */
public class StringUtils {
    public static boolean isEmpty(String str) {
        return null == str || str.isEmpty();
    }
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }


    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
