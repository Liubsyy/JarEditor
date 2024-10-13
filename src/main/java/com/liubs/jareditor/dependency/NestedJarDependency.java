package com.liubs.jareditor.dependency;

import com.liubs.jareditor.filetree.NestedJar;
import com.liubs.jareditor.util.JarUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 嵌套jar依赖，读取上层jar下面的所有嵌套jar作为依赖
 * @author Liubsyy
 * @date 2024/10/13
 */
public class NestedJarDependency implements IDependencyHandler{
    private NestedJar nestedJar;

    public NestedJarDependency(NestedJar nestedJar) {
        this.nestedJar = nestedJar;
    }

    @Override
    public List<String> dependentClassPaths(String jarPath, String dependencyRootPath) {
        List<String> destPaths = new ArrayList<>();
        try (JarFile jarFile = new JarFile(nestedJar.getParentPath())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if(entryName.endsWith(".jar")) {
                    Path destPath = Paths.get(dependencyRootPath, entry.toString());
                    JarUtil.createFile(jarFile,entry,destPath);
                    destPaths.add(destPath.toString());
                }
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return destPaths;
    }

    @Override
    public String replacePackage(String filePath, String packageName) {
        return packageName;
    }
}
