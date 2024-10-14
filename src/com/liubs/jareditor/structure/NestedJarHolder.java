package com.liubs.jareditor.structure;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Liubsyy
 * @date 2024/10/13
 */
public class NestedJarHolder {

    //所有展开的嵌套jar路径
    private Set<String> allExpandJars = Collections.synchronizedSet(new HashSet<>());

    public static NestedJarHolder getInstance(@NotNull Project project) {
        return project.getService(NestedJarHolder.class);
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
