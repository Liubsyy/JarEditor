package com.liubs.jareditor.util;

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
}
