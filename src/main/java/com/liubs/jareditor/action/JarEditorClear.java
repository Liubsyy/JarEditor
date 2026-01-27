package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.constant.JarLikeSupports;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.MyFileUtil;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;

/**
 * 删除Save的临时文件夹
 * @author Liubsyy
 * @date 2024/6/26
 */
public class JarEditorClear extends MyToolbarAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = super.currentEditorFile(e);
        if(null == selectedFile) {
            return;
        }

        final String jarPath = JarLikeSupports.FILE_EXT.contains(selectedFile.getExtension()) ? selectedFile.getPath(): MyPathUtil.getJarPathFromJar(selectedFile.getPath());
        if(null == jarPath) {
            NoticeInfo.warning("This operation only in JAR !!!");
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Clear temp directory ...", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {

                    //删除临时保存的目录
                    MyFileUtil.deleteDir(MyPathUtil.getJarEditTemp(selectedFile.getPath()));

                    NoticeInfo.info("Clear success !");
                }catch (Throwable e) {
                    NoticeInfo.error("Clear files err",e);
                }
            }
        });

    }
}
