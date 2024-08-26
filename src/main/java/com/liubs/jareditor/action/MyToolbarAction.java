package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.sdk.NoticeInfo;

/**
 * @author Liubsyy
 * @date 2024/8/27
 */
public abstract class MyToolbarAction extends AnAction {

    public VirtualFile currentEditorFile(AnActionEvent e){
        Project project = e.getProject();
        if(project == null) {
            NoticeInfo.warning("Please open a project");
            return null;
        }
        VirtualFile selectedFile = null;
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        if(null != fileEditorManager) {
            VirtualFile[] editorSelectFiles = fileEditorManager.getSelectedFiles();
            if(editorSelectFiles.length > 0) {
                selectedFile = editorSelectFiles[0];
            }
        }
        if(null == selectedFile) {
            NoticeInfo.warning("No editor opened");
            return null;
        }
        return selectedFile;
    }
}
