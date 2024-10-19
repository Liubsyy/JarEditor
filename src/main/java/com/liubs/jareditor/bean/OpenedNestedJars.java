package com.liubs.jareditor.bean;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 已经展开的jar
 * @author Liubsyy
 * @date 2024/10/13
 */
public class OpenedNestedJars {

    //所有展开的嵌套jar路径
    private Set<String> allExpandJars = Collections.synchronizedSet(new HashSet<>());

    public static OpenedNestedJars getInstance(@NotNull Project project) {
        return project.getService(OpenedNestedJars.class);
    }

    public void addExpandPath(String path){
        allExpandJars.add(path);
    }
    public void removeExpandPath(String path){
        allExpandJars.remove(path);
    }
    public boolean containsPath(String path){
        return allExpandJars.contains(path);
    }

}
