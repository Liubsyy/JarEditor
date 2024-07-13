package com.liubs.jareditor.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/**
 * @author Liubsyy
 * @date 2024/6/25
 */
public class CommandTools {

    private static String LINE_SEPARATOR = System.getProperty("line.separator");
    static {
        if(null == LINE_SEPARATOR) {
            LINE_SEPARATOR = "\n";
        }
    }

    public static String exec(String... commands){
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        try {
            Process process = processBuilder.start();
            StringBuilder resultBuilder = new StringBuilder();
            try (BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = stdOutReader.readLine()) != null) {
                    resultBuilder.append(line).append(LINE_SEPARATOR);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            int exitCode = process.waitFor();
            if(exitCode == 0) {
                return resultBuilder.toString();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(exec("/Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home/bin/javac","-version"));
    }
}
