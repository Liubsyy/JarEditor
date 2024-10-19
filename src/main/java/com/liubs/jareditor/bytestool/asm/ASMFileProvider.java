package com.liubs.jareditor.bytestool.asm;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.bean.OpenedASMEditors;
import org.jetbrains.annotations.NotNull;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class ASMFileProvider implements FileEditorProvider {
    public static final String EDITOR_TYPE_ID = "liubsyy-class-editor";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return "class".equalsIgnoreCase(file.getExtension())
                && OpenedASMEditors.getInstance(project).containsPath(file.getPath());
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new ASMEditor(project,file);
    }


    @NotNull
    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override
    public @NotNull
    FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }



}
