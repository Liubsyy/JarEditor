package com.liubs.jareditor.decompile;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Liubsyy
 * @date 2024/10/8
 */
public interface IDecompiler {
    String decompile(Project project,VirtualFile virtualFile);
}
