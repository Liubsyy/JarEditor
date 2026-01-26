package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.structure.ShowSizeDialog;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;


/**
 * @author Liubsyy
 * @date 2025/7/27
 */
public class ShowEntrySize extends AnAction {

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

        boolean isJarRoot = "jar".equals(selectedFile.getExtension()) && !selectedFile.getPath().contains(".jar!/");
        final String jarPath = isJarRoot ?
                selectedFile.getPath().replace(".jar!/",".jar") : MyPathUtil.getJarPathFromJar(selectedFile.getPath());
        if(null == jarPath) {
            NoticeInfo.warning("This operation only in JAR !!!");
            return;
        }

        String entryPathFromJar = MyPathUtil.getEntryPathFromJar(selectedFile.getPath());

        ShowSizeDialog showSizeDialog = new ShowSizeDialog(jarPath,entryPathFromJar);
        showSizeDialog.show();

    }
}
