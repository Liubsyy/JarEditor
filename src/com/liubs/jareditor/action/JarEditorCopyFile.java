package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.clipboard.CopyResult;
import com.liubs.jareditor.clipboard.FileToClipBoard;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.JarUtil;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 拷贝文件到剪切板
 * @author Liubsyy
 * @date 2024/6/2
 */
public class JarEditorCopyFile extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        //支持多选
        VirtualFile[] selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

        if(e.getProject() == null) {
            NoticeInfo.warning("Please open a project");
            return;
        }
        if(null == selectedFiles) {
            NoticeInfo.warning("No file selected");
            return;
        }

        Set<String> copyEntries = new HashSet<>();
        for (VirtualFile file : selectedFiles) {
            if(!file.getPath().contains(".jar!/")) {
                NoticeInfo.warning("Ony files in JAR can be copy !!!");
                return;
            }
            String entryPathFromJar = MyPathUtil.getEntryPathFromJar(file.getPath());
            if(null != entryPathFromJar) {
                if(file.isDirectory()) {
                    copyEntries.add(entryPathFromJar.replace("\\", "/")+"/");
                }else {
                    copyEntries.add(entryPathFromJar.replace("\\", "/"));
                }

            }
        }

        if(copyEntries.isEmpty()) {
            NoticeInfo.warning("Please select any file to copy!!");
            return;
        }

        final String clipboardPath = MyPathUtil.getFILE_TO_CLIPBOARD(selectedFiles[0].getPath());
        final String jarPath = MyPathUtil.getJarPathFromJar(selectedFiles[0].getPath());

        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Copy files to clipboard...", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    java.util.List<File> files = JarUtil.copyJarEntries(jarPath, clipboardPath, copyEntries);

                    if(files.isEmpty()) {
                        NoticeInfo.warning("Nothing copied !!!");
                        return;
                    }

                    CopyResult copyResult = FileToClipBoard.copyFilesToClipboard(files);
                    if(!copyResult.isSuccess()) {
                        NoticeInfo.error(copyResult.getError());
                        return;
                    }
                    NoticeInfo.info("Copy successfully, you can paste to another place from clipboard now !!!");
                }catch (Throwable e) {
                    NoticeInfo.error("Copy files to clipboard err",e.getMessage());
                }
            }
        });

    }
}
