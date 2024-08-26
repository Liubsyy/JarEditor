package com.liubs.jareditor.sdk;

/**
 * @author Liubsyy
 * @date 2024/5/9
 */
import com.intellij.notification.*;
import com.intellij.openapi.ui.MessageType;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NoticeInfo {
    private static NotificationGroup notificationGroup = NotificationGroupManager.getInstance()
            .getNotificationGroup("JarEditorNotice");


    public static void info(String message){
        Notification notification = notificationGroup.createNotification(message, MessageType.INFO);
        Notifications.Bus.notify(notification);
    }
    public static void info(String message,Object ...param){
        if(message == null || message.isEmpty()) {
            return;
        }
        message = String.format(message,param);
        Notification notification = notificationGroup.createNotification(message, MessageType.INFO);
        Notifications.Bus.notify(notification);
    }
    public static void errorWithoutFormat(String message){
        Notification notification = notificationGroup.createNotification(message, MessageType.ERROR);
        Notifications.Bus.notify(notification);
    }
    public static void error(String message){
        Notification notification = notificationGroup.createNotification(message, MessageType.ERROR);
        Notifications.Bus.notify(notification);
    }

    public static void error(String message,Object ...param){
        if(message == null || message.isEmpty()) {
            return;
        }
        message = String.format(message,param);
        Notification notification = notificationGroup.createNotification(message, MessageType.ERROR);
        Notifications.Bus.notify(notification);
    }

    public static void warning(String message){
        Notification notification = notificationGroup.createNotification(message, MessageType.WARNING);
        Notifications.Bus.notify(notification);
    }
    public static void warning(String message,Object ...param){
        if(message == null || message.isEmpty()) {
            return;
        }
        message = String.format(message,param);
        Notification notification = notificationGroup.createNotification(message, MessageType.WARNING);
        Notifications.Bus.notify(notification);
    }

    public static void auto(String message,boolean enable){
        Notification notification = notificationGroup.createNotification(message, enable ? MessageType.INFO: MessageType.ERROR);
        Notifications.Bus.notify(notification);
    }


}
