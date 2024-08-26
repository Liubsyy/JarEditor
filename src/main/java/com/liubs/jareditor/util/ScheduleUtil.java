package com.liubs.jareditor.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Liubsyy
 * @date 2024/6/3
 */
public class ScheduleUtil {
    private static ScheduledExecutorService
            scheduledExecutor = Executors.newScheduledThreadPool(5);


    public static void schedule(Runnable runnable,long seconds) {
        scheduledExecutor.schedule(runnable,seconds, TimeUnit.SECONDS);
    }
}
