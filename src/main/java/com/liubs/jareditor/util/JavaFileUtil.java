package com.liubs.jareditor.util;

import org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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


    /**
     * 根据class文件获取全名
     * @param classFilePath class文件路径
     * @return a/b/c这种格式
     */
    public static String getFullClassName(String classFilePath) {
        try (FileInputStream fis = new FileInputStream(classFilePath)){
            ClassReader classReader = new ClassReader(fis);
            return classReader.getClassName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据class获取内部类+本类
     * @param classFilePath
     * @return
     */
    public static List<String> getFullClassFiles(String classFilePath) {
        String extractClassEntryPath = classFilePath.substring(0,classFilePath.length()-".class".length());
        List<String> result = new ArrayList<>();
        File file = new File(classFilePath).getParentFile();
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            if(null != files) {
                for(File subFile : files) {
                    String path = subFile.getPath().replace("\\", "/");
                    if(path.endsWith(".class") ) {
                        if(path.equals(classFilePath) || path.startsWith(extractClassEntryPath+"$")) {
                            result.add(subFile.getPath());
                        }
                    }
                }
            }
        }

        return result;
    }


}
