package com.liubs.jareditor.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Liubsyy
 * @date 2024/5/19
 */
public class JavaFileUtil {

    private static Pattern packagePattern = Pattern.compile("^\\s*package\\s+([\\w.]+)\\s*;", Pattern.MULTILINE);

    /**
     * 根据源码获取包名
     * @param src
     * @return
     */
    public static String extractPackageName(String src) {
        Matcher matcher = packagePattern.matcher(src);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }



}
