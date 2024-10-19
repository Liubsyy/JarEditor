package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.bytestool.asm.ASMFileProvider;
import com.liubs.jareditor.bean.OpenedASMEditors;
import org.jetbrains.annotations.NotNull;


/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class ASMAction extends MyToolbarAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = super.currentEditorFile(e);
        if(null == selectedFile) {
            return;
        }
        FileEditorManager editorManager = FileEditorManager.getInstance(e.getProject());
        FileEditor[] editors = editorManager.getEditors(selectedFile);

        // 关闭现有的编辑器
        if (editors.length > 0) {
            editorManager.closeFile(selectedFile);
        }
        OpenedASMEditors.getInstance(e.getProject()).addPath(selectedFile.getPath());

        // 重新打开文件以触发编辑器的创建
        editorManager.openFile(selectedFile, true);
        editorManager.setSelectedEditor(selectedFile, ASMFileProvider.EDITOR_TYPE_ID);

    }

}
