package com.liubs.jareditor.sdk;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

/**
 * 动态加载编译器的库
 * @author Liubsyy
 * @date 2024/5/15
 */
public class JDKCompilerClassLoader extends URLClassLoader {
    public JDKCompilerClassLoader(String javaHome) {
        super(getURLs(javaHome));
    }

    private static URL[] getURLs(String libHome){
        File jdkLibDir = new File(libHome, "lib");
        File[] jarFiles = jdkLibDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null) {
            throw new IllegalStateException("No JAR files found in JDK lib directory.");
        }

        URL[] urls = Arrays.stream(jarFiles)
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .toArray(URL[]::new);
        return urls;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // 优先加载URL中的类
        try {
            Class<?> clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            // 类未找到，委托给父加载器
            return super.loadClass(name, resolve);
        }
    }
}
