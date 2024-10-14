package com.liubs.jareditor.bytestool;


import com.liubs.jareditor.util.JarUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成的class文件，连同内部类文件一起生成
 * @author Liubsyy
 * @date 2024/9/15
 */
public class ToClassFile {

    /**
     * jar目录
     */
    private String jarPath;

    /**
     * 类名路径
     */
    private String classEntryName;

    /**
     * 生成目录
     */
    private String targetPath;

    /**
     * 新文件覆盖拷贝文件
     */
    private Map<String,byte[]> coverClass = new HashMap<>();

    public ToClassFile(String jarPath, String classEntryName, String targetPath) {
        this.jarPath = jarPath;
        this.classEntryName = classEntryName;
        this.targetPath = targetPath;
    }


    public void addCoverFile(String className,byte[] bytes) {
        coverClass.put(className,bytes);
    }

    public void writeFiles() throws IOException {

        //拷贝jar内class和内部类到目标目录
        JarUtil.copyJarClassEntries(jarPath, targetPath,classEntryName);

        //拷贝新生成的coverClass到指定目录
        String relaDir = classEntryName.substring(0,classEntryName.lastIndexOf("/")+1);
        for(Map.Entry<String,byte[]> entry : coverClass.entrySet()) {
            String simpleClassName = entry.getKey();
            int lastIndexOf = simpleClassName.lastIndexOf(".");
            if(lastIndexOf > 0) {
                simpleClassName = simpleClassName.substring(lastIndexOf+1);
            }

            Files.write(Paths.get(targetPath,relaDir,simpleClassName+".class"),
                    entry.getValue());
        }
    }


}
