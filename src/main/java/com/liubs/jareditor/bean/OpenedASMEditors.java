package com.liubs.jareditor.bean;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 已经打开过的asm编辑器
 * @author Liubsyy
 * @date 2024/10/19
 */
public class OpenedASMEditors {
    
    private Set<String> allPaths = Collections.synchronizedSet(new HashSet<>());

    public static OpenedASMEditors getInstance(@NotNull Project project) {
        return project.getService(OpenedASMEditors.class);
    }

    public void addPath(String path){
        allPaths.add(path);
    }
    public void removePath(String path){
        allPaths.remove(path);
    }
    public boolean containsPath(String path){
        return allPaths.contains(path);
    }

}