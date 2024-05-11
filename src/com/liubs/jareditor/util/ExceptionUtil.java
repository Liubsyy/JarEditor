package com.liubs.jareditor.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Liubsyy
 * @date 2023/8/29
 */
public class ExceptionUtil {

    public static String getExceptionTracing(Throwable ex){
        if(ex==null)
            return "";
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        ex.printStackTrace(pw);
        String stackTraceString = sw.getBuffer().toString();
        return stackTraceString;
    }
}
