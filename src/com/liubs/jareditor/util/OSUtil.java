package com.liubs.jareditor.util;

/**
 * @author Liubsyy
 * @date 2024/5/10
 */
public class OSUtil {

    public static String getOS(){
        String osName = System.getProperty("os.name").toLowerCase();
        return osName;
    }

    public static boolean isWindows(){
        return getOS().contains("windows");
    }
}
