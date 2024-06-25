package com.liubs.jareditor.sdk;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.liubs.jareditor.persistent.SDKSettingStorage;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取项目JDK的 JavaCompiler
 * @author Liubsyy
 * @date 2024/5/9
 */
public class JavacToolProvider {

    // 定义正则表达式模式以提取版本号
    private static final Pattern JAVA_VERSION_PATTERN = Pattern.compile("(\\d+)(\\.\\d+)*");

    public static JavaCompiler getJavaCompilerFromProjectSdk() {

        try {
            Class<?> javacToolClass = Class.forName("com.sun.tools.javac.api.JavacTool");
            return (JavaCompiler) javacToolClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
    public static int getMaxJdkVersion() {

        //偏好设置有最大值的话取最大值
        int maxJavaVersion = SDKSettingStorage.getInstance().getMaxJavaVersion();
        if(maxJavaVersion > 0) {
            return maxJavaVersion;
        }

        //从sdk列表中取最大值
        int maxVersion = -1;
        Sdk[] allJdks = ProjectJdkTable.getInstance().getAllJdks();
        for(Sdk sdk : allJdks) {
            if(sdk!=null && sdk.getSdkType() instanceof JavaSdk) {
                maxVersion = Math.max(maxVersion,  parseJavaVersion(sdk.getVersionString()));
            }
        }
        return maxVersion;
    }

    public static int parseJavaVersion(String versionString) {
        Matcher matcher = JAVA_VERSION_PATTERN.matcher(versionString);
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
