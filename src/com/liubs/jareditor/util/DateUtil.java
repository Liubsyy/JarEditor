package com.liubs.jareditor.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Liubsyy
 * @date 2025/5/23
 */
public class DateUtil {
    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String formatDate(Date date) {
       return formatDate(date,DATE_PATTERN);
    }
    public static String formatDate(Date date,String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String returnValue = sdf.format(date);
        return returnValue;
    }

    public static Date parseDate(String dateStr) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        return sdf.parse(dateStr);
    }

    public static Date addSecond(Date date, int second){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, second);
        return calendar.getTime();
    }


}
