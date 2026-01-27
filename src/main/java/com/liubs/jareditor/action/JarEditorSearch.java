package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.liubs.jareditor.constant.JarLikeSupports;
import com.liubs.jareditor.search.JarFileSearchDialog;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;

/**
 * jar包内搜索文件和字符串
 * @author Liubsyy
 * @date 2024/6/25
 */
public class JarEditorSearch extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {

        Project project = e.getProject();
        if(project == null) {
            NoticeInfo.warning("Please open a project");
            return;
        }
        VirtualFile selectedFile = null;
        if( ActionPlaces.TOOLBAR.equals(e.getPlace())) {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            VirtualFile[] editorSelectFiles = fileEditorManager.getSelectedFiles();
            if(editorSelectFiles.length > 0) {
                selectedFile = editorSelectFiles[0];
            }
        }else {
            selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        }

        VirtualFile jarRoot = null;
        if(null != selectedFile) {
            final String jarPath = JarLikeSupports.FILE_EXT.contains(selectedFile.getExtension()) ?
                    selectedFile.getPath() : MyPathUtil.getJarPathFromJar(selectedFile.getPath());
            if(null != jarPath) {
                jarRoot = VirtualFileManager.getInstance().findFileByUrl("jar://" + jarPath + "!/");
            }
        }


        JarFileSearchDialog jarFileSearchDialog = new JarFileSearchDialog(e.getProject(),jarRoot);
        jarFileSearchDialog.show();
    }
}
