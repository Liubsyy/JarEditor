package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.liubs.jareditor.editor.JarFileSearchDialog;
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
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if(null == selectedFile) {
            NoticeInfo.warning("No file selected");
            return;
        }

        final String jarPath =  MyPathUtil.getJarPathFromJar(selectedFile.getPath());
        if(null == jarPath) {
            NoticeInfo.warning("This operation only in JAR !!!");
            return;
        }
        VirtualFile jarRoot = VirtualFileManager.getInstance().findFileByUrl("jar://" + jarPath + "!/");
        JarFileSearchDialog jarFileSearchDialog = new JarFileSearchDialog(e.getProject(),jarRoot);
        jarFileSearchDialog.show();
    }
}
