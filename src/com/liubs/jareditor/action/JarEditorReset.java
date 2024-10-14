package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.editor.MyJarEditor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reset Editor
 * @author Liubsyy
 * @date 2024/9/2
 */
public class JarEditorReset extends MyToolbarAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = super.currentEditorFile(e);
        if(null == selectedFile) {
            return;
        }

        List<FileEditor> editors = Arrays.stream(FileEditorManager.getInstance(e.getProject()).getEditors(selectedFile))
                .filter(fileEditor -> fileEditor instanceof MyJarEditor)
                .collect(Collectors.toList());
        if(editors.isEmpty()) {
            return;
        }
        MyJarEditor myJarEditor = (MyJarEditor)editors.get(0);
        myJarEditor.resetEditorContent();
    }
}
