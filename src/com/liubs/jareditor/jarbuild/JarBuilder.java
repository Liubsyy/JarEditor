package com.liubs.jareditor.jarbuild;

import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.ExceptionUtil;
import com.liubs.jareditor.util.JarUtil;
import com.liubs.jareditor.util.Md5Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;


/**
 * @author Liubsyy
 * @date 2024/5/9
 */
public class JarBuilder {

    private String classDictionary;
    private String jarFile;

    public JarBuilder(String classDictionary, String jarFile) {
        this.classDictionary = classDictionary;
        this.jarFile = jarFile;
    }

    public JarBuilder(String jarFile) {
        this.jarFile = jarFile;
    }

    public JarBuildResult writeJar(boolean compareEntry) {

        JarBuildResult jarBuildResult;
        File tempJarFile = null;
        try {
            Path jarPath = Paths.get(jarFile);
            Path classDirPath = Paths.get(classDictionary);

            if (!Files.exists(jarPath)) {
                return new JarBuildResult(false, "File does not exist: " + jarFile);
            }


            Map<String, byte[]> oldEntryMap = null;
            if(compareEntry) {
                oldEntryMap = JarUtil.readEntryData(jarFile);
            }

            if (!Files.exists(classDirPath)) {
                return new JarBuildResult(false, "Dictionary does not exist: " + classDictionary);
            }

            // 临时文件替代内存流
            tempJarFile = Files.createTempFile("tempJar", ".jar").toFile();

            try (JarFile originalJar = new JarFile(jarFile);
                 JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile))) {

                copyExistingEntries(originalJar, tempJarOutputStream, classDirPath);
                addOrUpdateClasses(classDirPath, tempJarOutputStream);
            }

            // 将临时 JAR 文件内容写回目标 JAR 文件
            writeTargetJar(tempJarFile);

            Map<String, byte[]> newEntryMap = null;
            if(compareEntry) {

                NoticeInfo.info("*** Compare difference :"+jarFile+" START ***");
                newEntryMap = JarUtil.readEntryData(jarFile);

                NoticeInfo.info("Old jar size:"+oldEntryMap.size());
                NoticeInfo.info("New jar size:"+newEntryMap.size());

                for(Map.Entry<String,byte[]> entry : oldEntryMap.entrySet()) {
                    byte[] bytes = newEntryMap.get(entry.getKey());
                    if(null == bytes) {
                        NoticeInfo.error("Delete entry:"+entry.getKey());
                        continue;
                    }

                    if(Md5Util.md5(entry.getValue()).equals(Md5Util.md5(bytes))) {
                        NoticeInfo.info("Entry is same:"+entry.getKey());
                    }else {
                        NoticeInfo.error("Entry modified:"+entry.getKey());
                    }
                }
                for(Map.Entry<String,byte[]> entry : newEntryMap.entrySet()) {
                    if(!oldEntryMap.containsKey(entry.getKey())) {
                        NoticeInfo.info("Add entry:"+entry.getKey());
                    }
                }
                NoticeInfo.info("*** Compare difference :"+jarFile+" END ***");

            }
            jarBuildResult = new JarBuildResult(true, null);
        } catch (IOException e) {
            jarBuildResult = new JarBuildResult(false, "Build jar failed: " + ExceptionUtil.getExceptionTracing(e));
        } finally {
            if (tempJarFile != null && tempJarFile.exists()) {
                tempJarFile.delete(); // 删除临时文件
            }
        }
        return jarBuildResult;
    }


    /**
     * 在jar内新增一个空文件以便写代码
     * @param filePath
     * @return
     */
    public JarBuildResult addFile(String filePath){
        JarBuildResult jarBuildResult;
        File tempJarFile = null;
        try {
            Path jarPath = Paths.get(jarFile);

            if (!Files.exists(jarPath)) {
                return new JarBuildResult(false, "File does not exist: " + jarFile);
            }

            // 临时文件替代内存流
            tempJarFile = Files.createTempFile("tempJar", ".jar").toFile();

            try (JarFile originalJar = new JarFile(jarFile);
                JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile))) {
                copyExistingEntries(originalJar, tempJarOutputStream, new HashSet<>(),true);

                //write a empty file
                String jarEntryName = filePath.replace("\\", "/");
                tempJarOutputStream.putNextEntry(new JarEntry(jarEntryName));
                tempJarOutputStream.closeEntry();
            }

            // 将临时 JAR 文件内容写回目标 JAR 文件
            writeTargetJar(tempJarFile);
            jarBuildResult = new JarBuildResult(true, null);
        } catch (IOException e) {
            jarBuildResult = new JarBuildResult(false, "Build jar failed: " + ExceptionUtil.getExceptionTracing(e));
        } finally {
            if (tempJarFile != null && tempJarFile.exists()) {
                tempJarFile.delete(); // 删除临时文件
            }
        }
        return jarBuildResult;
    }

    public JarBuildResult deleteFiles(Set<String> deleteEntries){
        JarBuildResult jarBuildResult;
        File tempJarFile = null;
        try {
            Path jarPath = Paths.get(jarFile);

            if (!Files.exists(jarPath)) {
                return new JarBuildResult(false, "File does not exist: " + jarFile);
            }

            // 临时文件替代内存流
            tempJarFile = Files.createTempFile("tempJar", ".jar").toFile();

            try (JarFile originalJar = new JarFile(jarFile);
                 JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile))) {
                copyExistingEntries(originalJar, tempJarOutputStream, deleteEntries,true);
            }

            // 将临时 JAR 文件内容写回目标 JAR 文件
            writeTargetJar(tempJarFile);
            jarBuildResult = new JarBuildResult(true, null);
        } catch (IOException e) {
            jarBuildResult = new JarBuildResult(false, "Build jar failed: " + ExceptionUtil.getExceptionTracing(e));
        } finally {
            if (tempJarFile != null && tempJarFile.exists()) {
                tempJarFile.delete(); // 删除临时文件
            }
        }
        return jarBuildResult;
    }
    public JarBuildResult renameFile(String entryPath,String newEntryPath ,boolean isDictionary){
        JarBuildResult jarBuildResult;
        File tempJarFile = null;
        try {
            Path jarPath = Paths.get(jarFile);

            if (!Files.exists(jarPath)) {
                return new JarBuildResult(false, "File does not exist: " + jarFile);
            }
            tempJarFile = Files.createTempFile("tempJar", ".jar").toFile();

            try (JarFile originalJar = new JarFile(jarFile);
                 JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tempJarFile))) {

                Enumeration<JarEntry> entries = originalJar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();

                    JarEntry newEntry ;

                    //如果是目录，子目录和子文件的路径都应该改变
                    if (isDictionary) {
                        //这里不会有问题，因为文件夹是以/结尾
                        if(entry.getName().startsWith(entryPath)) {
                            newEntry = copyNewEntry(originalJar,
                                    entry, entry.getName().replace(entryPath,newEntryPath));
                        }else {
                            newEntry = copyNewEntry(originalJar,entry);
                        }
                    }else {
                        if(entry.getName().equals(entryPath)) {
                            newEntry = copyNewEntry(originalJar,entry,newEntryPath);
                        }else {
                            newEntry = copyNewEntry(originalJar,entry);
                        }
                    }


                    tempJarOutputStream.putNextEntry(newEntry);

                    try (InputStream entryInputStream = originalJar.getInputStream(entry)) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                            tempJarOutputStream.write(buffer, 0, bytesRead);
                        }
                    }

                    tempJarOutputStream.closeEntry();
                }

            }

            // 将临时 JAR 文件内容写回目标 JAR 文件
            writeTargetJar(tempJarFile);
            jarBuildResult = new JarBuildResult(true, null);
        } catch (IOException e) {
            jarBuildResult = new JarBuildResult(false, "Rename failed: " + ExceptionUtil.getExceptionTracing(e));
        } finally {
            if (tempJarFile != null && tempJarFile.exists()) {
                tempJarFile.delete(); // 删除临时文件
            }
        }
        return jarBuildResult;
    }

    private void writeTargetJar(File tempJarFile) throws IOException {
        // 将临时 JAR 文件内容写回目标 JAR 文件
        try (FileInputStream tempInputStream = new FileInputStream(tempJarFile);
             FileOutputStream fileOutputStream = new FileOutputStream(jarFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = tempInputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        }
    }


    private void copyExistingEntries(JarFile originalJar, JarOutputStream tempJarOutputStream, Path classesDir) throws IOException {
        Set<String> classesToReplace = new HashSet<>();
        Files.walk(classesDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    String relativePath = classesDir.relativize(path).toString().replace("\\", "/");
                    classesToReplace.add(relativePath);
                });
        copyExistingEntries(originalJar,tempJarOutputStream,classesToReplace,false);

    }

    private JarEntry copyNewEntry(JarFile originalJar,JarEntry entry, String newEntryName) throws IOException {

        JarEntry newEntry = new JarEntry(newEntryName);
        newEntry.setTime(entry.getTime());
        // 如果原条目使用 STORED 方法，需要显式设置大小、压缩大小和 CRC-32
        if (entry.getMethod() == JarEntry.STORED) {
            long size = entry.getSize();
            long compressedSize = entry.getCompressedSize();
            long crc = entry.getCrc();

            if (size == -1 || compressedSize == -1 || crc == -1) {
                CRC32 crc32 = new CRC32();
                long computedSize = 0;

                try (InputStream entryInputStream = originalJar.getInputStream(entry)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                        crc32.update(buffer, 0, bytesRead);
                        computedSize += bytesRead;
                    }
                }

                size = computedSize;
                compressedSize = computedSize;
                crc = crc32.getValue();
            }

            newEntry.setSize(size);
            newEntry.setCompressedSize(compressedSize);
            newEntry.setCrc(crc);
            newEntry.setMethod(JarEntry.STORED);

            //NoticeInfo.error("STORED ENTRY:"+entry);
        } else {
            newEntry.setMethod(JarEntry.DEFLATED);
        }
        return newEntry;
    }
    private JarEntry copyNewEntry(JarFile originalJar,JarEntry entry) throws IOException {
        return copyNewEntry(originalJar,entry,entry.getName());
    }

    private void copyExistingEntries(JarFile originalJar, JarOutputStream tempJarOutputStream, Set<String> excludeEntries,boolean resoleDir) throws IOException {
        Enumeration<JarEntry> entries = originalJar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();

            if(resoleDir) {
                boolean needCopy = true;
                for(String excludeEntry : excludeEntries) {
                    if(entry.getName().startsWith(excludeEntry)) {
                        needCopy = false;
                        break;
                    }
                }
                //删除的文件或者文件夹不需要copy
                if( !needCopy ) {
                    continue;
                }
            }else {
                if(excludeEntries.contains(entry.getName())) {
                    continue;
                }
            }
            JarEntry newEntry = copyNewEntry(originalJar,entry);
            tempJarOutputStream.putNextEntry(newEntry);

            try (InputStream entryInputStream = originalJar.getInputStream(entry)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                    tempJarOutputStream.write(buffer, 0, bytesRead);
                }
            }

            tempJarOutputStream.closeEntry();
        }
    }

    private void addOrUpdateClasses(Path classesDir, JarOutputStream tempJarOutputStream) throws IOException {
        Files.walk(classesDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    String jarEntryName = classesDir.relativize(path).toString().replace("\\", "/");
                    try {
                        tempJarOutputStream.putNextEntry(new JarEntry(jarEntryName));
                        Files.copy(path, tempJarOutputStream);
                        tempJarOutputStream.closeEntry();
                    } catch (IOException e) {
                        System.err.println("Error adding/updating class: " + jarEntryName);
                        e.printStackTrace();
                    }
                });
    }
}
