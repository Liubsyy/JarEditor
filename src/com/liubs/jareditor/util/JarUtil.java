package com.liubs.jareditor.util;

import com.liubs.jareditor.sdk.NoticeInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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

}
