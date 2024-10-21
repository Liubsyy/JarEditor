package com.liubs.jareditor.bytestool.asm;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.bytestool.asm.ui.ASMEditorPanel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class ASMEditor extends UserDataHolderBase implements FileEditor {
    private final Project project;
    private final ASMEditorPanel mainPanel;
    private final VirtualFile file;

    public ASMEditor(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;
        this.mainPanel = new ASMEditorPanel(project,file);
    }

    @Nullable
    @Override
    public VirtualFile getFile() {
        return file;
    }


    @Override
    public @NotNull
    JComponent getComponent() {
        return mainPanel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public @NonNls
    @NotNull String getName() {
        return "ASM Editor";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {}

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return file.isValid();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {}

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {}

    @Override
    public @Nullable
    FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {
        try{

        }catch (Throwable e) {}

    }




}
