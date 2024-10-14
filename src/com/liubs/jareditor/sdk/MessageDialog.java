package com.liubs.jareditor.sdk;

import com.intellij.openapi.ui.Messages;

public class MessageDialog {

    public static String addLineBreaks(String text, int maxLength) {
        StringBuilder result = new StringBuilder();
        int index = 0;
        while (index < text.length()) {
            // 找到下一个要插入换行的位置
            int nextBreak = Math.min(index + maxLength, text.length());
            result.append(text, index, nextBreak);
            // 如果不是最后一行，添加换行符
            if (nextBreak < text.length()) {
                result.append("\n");
            }
            index = nextBreak;
        }
        return result.toString();
    }

    public static void showMessageDialog(String title,String message){
        Messages.showMessageDialog( addLineBreaks(message,50), title,Messages.getInformationIcon());
    }
    public static void showErrorMessageDialog(String title,String message){
        Messages.showMessageDialog( addLineBreaks(message,50), title,Messages.getErrorIcon());
    }

    public static String showInputDialog(String title,String message) {
        String inputValue = Messages.showInputDialog(addLineBreaks(message,50), title, Messages.getQuestionIcon());
        return inputValue;
    }
}
