package com.liubs.jareditor.dependency;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * FatJar的依赖：需要添加 BOOT-INF/classes和BOOT-INF/lib下的依赖
 * -MainBoot.jar
 *   -BOOT-INF
 *     -classes
 *     -lib
 *   -META-INF
 *   -org.spring.framework.boot.loader
 *
 * 为什么不直接复用 FatJar的 URLStreamHandler实现动态添加依赖呢？
 * 首先本插件所有代码运行在IDEA自带JDK上，如果依赖的lib版本高于自带JDK的版本，是无法被动态加载的
 *
 * @author Liubsyy
 * @date 2024/5/19
 */
public class SpringBootDependency implements IDependencyHandler{


    public List<String> dependentClassPaths(String jarPath, String dependencyRootPath) {
        List<String> copiedFiles = new ArrayList<>();
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith("BOOT-INF/classes/") || entryName.startsWith("BOOT-INF/lib/")) {
                    Path targetPath = Paths.get(dependencyRootPath, entryName.substring("BOOT-INF/".length()));

                    if (entry.isDirectory()) {
                        Files.createDirectories(targetPath);
                    } else {
                        Files.createDirectories(targetPath.getParent());
                        try (InputStream inputStream = jarFile.getInputStream(entry);
                             OutputStream outputStream = Files.newOutputStream(targetPath)) {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                outputStream.write(buffer, 0, bytesRead);
                            }
                        }
                        if(entryName.startsWith("BOOT-INF/lib/")) {
                            copiedFiles.add(targetPath.toString());
                        }
                    }
                }
            }
            copiedFiles.add(dependencyRootPath+"/classes/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return copiedFiles;
    }

}
