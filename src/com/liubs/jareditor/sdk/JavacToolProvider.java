package com.liubs.jareditor.sdk;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取项目JDK的 JavaCompiler
 * @author Liubsyy
 * @date 2024/5/9
 */
public class JavacToolProvider {
    private static final Map<String, ClassLoader> classLoaderCache = new ConcurrentHashMap<>();

    public static JavaCompiler getJavaCompilerFromProjectSdk(Project project) {

        if(null != project) {
            Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if(null != projectSdk) {
                String javaHome = projectSdk.getHomePath();
                if (javaHome != null) {
                    try {
                        // 从缓存中获取 ClassLoader
                        ClassLoader projectJdkClassLoader = classLoaderCache.computeIfAbsent(javaHome, path -> {
                            try {
                                return new URLClassLoader(
                                        new URL[]{
                                                new File(path).toURI().toURL()
                                        },
                                        ClassLoader.getSystemClassLoader()
                                );
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });

                        // 使用缓存的 ClassLoader 加载 Java 编译器类
                        Class<?> javacToolClass = projectJdkClassLoader.loadClass("com.sun.tools.javac.api.JavacTool");
                        return (JavaCompiler) javacToolClass.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //实在没有的话，就用IDEA运行时依赖的jdk吧
        return ToolProvider.getSystemJavaCompiler();
    }

    /**
     * 获取项目使用的 JDK 版本
     *
     * @param project IntelliJ IDEA 项目
     * @return JDK 大版本号，例如 8, 11, 17 等
     */
    public static int getProjectJdkVersion(Project project) {
        // 获取项目使用的 JDK
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null && projectSdk.getSdkType() instanceof JavaSdk) {
            return parseJavaVersion(projectSdk.getVersionString());
        }

        return -1;
    }

    private static int parseJavaVersion(String versionString) {
        // 定义正则表达式模式以提取版本号
        String versionPattern = "(\\d+)(\\.\\d+)*";
        Pattern pattern = Pattern.compile(versionPattern);
        Matcher matcher = pattern.matcher(versionString);

        if (matcher.find()) {
            String[] versionParts = matcher.group().split("\\.");
            if (versionParts[0].equals("1")) {
                // 处理 Java 8 及之前的版本
                return Integer.parseInt(versionParts[1]);
            } else {
                // 处理 Java 9 及之后的版本
                return Integer.parseInt(versionParts[0]);
            }
        }

        // 如果未能匹配版本号，返回 -1 表示未知版本
        return -1;
    }
}
