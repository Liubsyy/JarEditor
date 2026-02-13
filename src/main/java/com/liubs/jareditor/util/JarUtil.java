package com.liubs.jareditor.util;

import com.liubs.jareditor.sdk.NoticeInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * @author Liubsyy
 * @date 2024/5/11
 */
public class JarUtil {

    /**
     * 读取jar的所有classes到Map
     * @param jarPath
     * @return
     */
    public static Map<String,byte[]> readJarClasses(String jarPath) throws IOException {
        byte[] jarBytes = Files.readAllBytes(Paths.get(jarPath));
        return readJarClasses(jarBytes);
    }
    public static Map<String,byte[]> readJarClasses(byte[] jarBytes) throws IOException {

        Map<String,byte[]> allClassBytes = new HashMap<>();
        try(JarInputStream jarInputStream = new JarInputStream(new ByteArrayInputStream(jarBytes));){
            JarEntry jarEntry;

            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                if ((jarEntry.getName().endsWith(".class"))) {
                    String className = jarEntry.getName().replaceAll("/", "\\.");
                    String myClass = className.substring(0, className.lastIndexOf('.'));

                    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                    int nextValue = jarInputStream.read();
                    while (nextValue > -1) {
                        byteStream.write(nextValue);
                        nextValue = jarInputStream.read();
                    }
                    byte[] classBytes = byteStream.toByteArray();
                    allClassBytes.put(myClass,classBytes);
                }
            }
        }
        return allClassBytes;
    }

    public static Map<String,byte[]> readJarClasses(String jarPath, String classFullName) throws IOException {
        byte[] jarBytes = Files.readAllBytes(Paths.get(jarPath));
        return readJarClasses(jarBytes,classFullName);
    }

    /**
     * 从 jar 的 byte[] 中提取指定类及其所有内部类
     *
     * @param jarBytes      jar 文件的字节数组
     * @param classFullName 类的完整名，如 "com.foo.Bar"
     * @return Map<entryName, classBytes>
     */
    public static Map<String, byte[]> readJarClasses(byte[] jarBytes, String classFullName) throws IOException {

        Map<String, byte[]> result = new LinkedHashMap<>();

        // 转成 jar entry 路径
        String basePath = classFullName.replace('.', '/');

        try (JarInputStream jis = new JarInputStream(new ByteArrayInputStream(jarBytes))) {
            JarEntry entry;

            while ((entry = jis.getNextJarEntry()) != null) {
                String name = entry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }

                // 精确匹配：
                // 1) 本类
                // 2) 所有内部类
                if (name.equals(basePath + ".class") ||
                        name.startsWith(basePath + "$")) {

                    byte[] bytes = readAllBytes(jis);
                    result.put(name, bytes);
                }
            }
        }

        return result;
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] tmp = new byte[4096];
        int len;
        while ((len = in.read(tmp)) != -1) {
            buffer.write(tmp, 0, len);
        }
        return buffer.toByteArray();
    }


    public static Map<String,byte[]> readEntryData(String jarPath) throws IOException {
        byte[] jarBytes = Files.readAllBytes(Paths.get(jarPath));
        Map<String,byte[]> allClassBytes = new HashMap<>();
        try(JarInputStream jarInputStream = new JarInputStream(new ByteArrayInputStream(jarBytes))){
            JarEntry jarEntry;

            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                if(jarEntry.getMethod() == JarEntry.STORED) {
                    NoticeInfo.error("STORED ENTRY:"+jarEntry);
                }
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                int nextValue = jarInputStream.read();
                while (nextValue > -1) {
                    byteStream.write(nextValue);
                    nextValue = jarInputStream.read();
                }
                byte[] classBytes = byteStream.toByteArray();
                allClassBytes.put(jarEntry.getName(),classBytes);
            }
        }
        return allClassBytes;
    }

    /**
     * 是否已经存在entry
     * @param jarFilePath
     * @param entryPath
     * @return
     * @throws IOException
     */
    public static boolean existEntry(String jarFilePath, String entryPath)  {
        try (JarFile jarFile = new JarFile(jarFilePath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().equals(entryPath)) {
                    return true;
                }
            }
        }catch (Throwable e) {
            return false;
        }
        return false;
    }


    public static List<File> copyJarEntries(String jarPath, String clipboardPath, Set<String> copyEntries) throws IOException {
        List<File> copiedFiles = new ArrayList<>();

        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                for (String copyEntry : copyEntries) {
                    if (entryName.startsWith(copyEntry)) {
                        if(entryName.equals(copyEntry)) {
                            //拷贝的文件
                            String entrySimpleName = getEntrySimpleName(entryName);
                            Path path = Paths.get(clipboardPath, entrySimpleName);
                            createFile(jarFile,entry,path);
                            copiedFiles.add(path.toFile());
                        }else {
                            //子目录和子文件
                            String simpleParentName = getEntrySimpleName(copyEntry);
                            createFile(jarFile,entry,
                                    Paths.get(clipboardPath,simpleParentName,entryName.replace(copyEntry,"") ));
                        }
                    }
                }
            }
        }
        return copiedFiles;
    }
    public static void copyJarRelativeEntries(String jarPath, String destPath, Set<String> copyEntries) throws IOException {
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                for (String copyEntry : copyEntries) {
                    if (entryName.startsWith(copyEntry)) {
                        Path path = Paths.get(destPath, entryName);
                        createFile(jarFile,entry,path);
                    }
                }
            }
        }
    }


    /**
     *  拷贝jar内class和内部类目标目录
     * @param jarPath
     * @param destDir
     * @param fullClassPath a/b/c这样的格式
     * @return
     * @throws IOException
     */
    public static List<File> copyJarClassEntries(String jarPath, String destDir, String fullClassPath) throws IOException {
        List<File> copiedFiles = new ArrayList<>();
        String extractClassEntryPath = fullClassPath.substring(0,fullClassPath.length()-".class".length());

        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if(!entryName.endsWith(".class") ){
                    continue;
                }

                if(entryName.equals(fullClassPath) || entryName.startsWith(extractClassEntryPath+"$")) {
                    String relaPath = fullClassPath.substring(0, fullClassPath.lastIndexOf("/"));
                    String entrySimpleName = getEntrySimpleName(entryName);
                    Path path = Paths.get(destDir,relaPath, entrySimpleName);
                    createFile(jarFile,entry,path);
                    copiedFiles.add(path.toFile());
                }
            }
        }
        return copiedFiles;
    }

    private static String getEntrySimpleName(String entryName){
        if(entryName.endsWith("/")) {
            String substring = entryName.substring(0, entryName.length() - 1);
            int i = substring.lastIndexOf("/");
            if(i>=0) {
                return substring.substring(i+1);
            }else {
                return substring;
            }
        }else {
            int i = entryName.lastIndexOf("/");
            if(i>=0) {
                return entryName.substring(i+1);
            }else {
                return entryName;
            }
        }
    }

    public static void createFile(JarFile jarFile,JarEntry entry, Path destinationPath) throws IOException {
        if (entry.isDirectory()) {
            Files.createDirectories(destinationPath);
        } else {
            try (InputStream inputStream = jarFile.getInputStream(entry)) {
                Files.createDirectories(destinationPath.getParent());
                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

}
