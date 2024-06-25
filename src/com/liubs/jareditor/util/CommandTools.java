package com.liubs.jareditor.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author Liubsyy
 * @date 2024/6/25
 */
public class CommandTools {
    public static String exec(String command) {

        try {
            // 执行外部命令
            Process process = Runtime.getRuntime().exec(command);

            // 获取标准输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String lineSeparator = System.getProperty("line.separator");
                if(null == lineSeparator) {
                    lineSeparator = "\n";
                }
                output.append(line).append(lineSeparator);
            }

            // 获取命令执行的退出状态
            int exitCode = process.waitFor();

            if(exitCode == 0) {
                return output.toString();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }
}
