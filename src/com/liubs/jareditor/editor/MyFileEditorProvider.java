package com.liubs.jareditor.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author Liubsyy
 * @date 2024/5/8
 */
public class MyFileEditorProvider implements FileEditorProvider {
    public static final String EDITOR_TYPE_ID = "liubsyy-jar-editor";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        // 只扩展jar内文件
        //return "class".equalsIgnoreCase(file.getExtension())
        return file.getPath().contains(".jar!/");
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new MyJarEditor(project, file);
    }


    @NotNull
    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }



}
