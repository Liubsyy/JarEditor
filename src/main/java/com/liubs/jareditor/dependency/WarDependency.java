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
 * @author Liubsyy
 * @date 2026/1/27
 */
public class WarDependency implements IDependencyHandler{

    @Override
    public List<String> dependentClassPaths(String jarPath, String dependencyRootPath) {
        List<String> copiedFiles = new ArrayList<>();
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith("WEB-INF/classes/") || entryName.startsWith("WEB-INF/lib/")) {
                    Path targetPath = Paths.get(dependencyRootPath, entryName.substring("WEB-INF/".length()));

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
                        if(entryName.startsWith("WEB-INF/lib/")) {
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

    @Override
    public String replacePackage(String filePath, String packageName) {
        if(filePath.contains("WEB-INF/classes/")) {
            return packageName.replace("WEB-INF.classes.", "");
        }
        if(filePath.contains("WEB-INF/lib/")) {
            return packageName.replace("WEB-INF.lib.", "");
        }
        return packageName;
    }

}
