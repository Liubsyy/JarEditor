package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.liubs.jareditor.jarbuild.JarBuilder;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Delete entries in jar
 * @author Liubsyy
 * @date 2024/5/12
 */
public class JarEditorDeleteFiles extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile[] selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

        if(e.getProject() == null) {
            NoticeInfo.warning("Please open a project");
            return;
        }
        if(null == selectedFiles) {
            NoticeInfo.warning("No file selected");
            return;
        }

        Set<String> deleteEntries = new HashSet<>();
        for (VirtualFile file : selectedFiles) {
            if(!file.getPath().contains(".jar!/")) {
                NoticeInfo.warning("Ony files in JAR can be deleted !!!");
                return;
            }
            String entryPathFromJar = MyPathUtil.getEntryPathFromJar(file.getPath());
            if(null != entryPathFromJar) {
                if(file.isDirectory()) {
                    //删除文件夹 /dir 导致/dir2 也删除的问题
                    deleteEntries.add(entryPathFromJar.replace("\\", "/")+"/");
                }else {
                    deleteEntries.add(entryPathFromJar.replace("\\", "/"));
                }

            }
        }

        int response = Messages.showYesNoDialog(
                e.getProject(),
                "Are you sure you want to delete "+deleteEntries+"?", // 消息内容
                "Confirmation", // 窗口标题
                Messages.getQuestionIcon() // 使用一个问号图标
        );

        if (response != Messages.YES) {
            return;
        }

        if(deleteEntries.isEmpty()) {
            NoticeInfo.warning("Please select any file to delete!!");
            return;
        }
        final String jarPath = MyPathUtil.getJarPathFromJar(selectedFiles[0].getPath());

        try{
            //close editors
            FileEditorManager editorManager = FileEditorManager.getInstance(e.getProject());
            VirtualFile[] openFiles = editorManager.getOpenFiles();
            for (VirtualFile file : openFiles) {
                if (file.getPath().startsWith(jarPath)) {
                    editorManager.closeFile(file);
                }
            }
        }catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Deleting files in JAR...", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    JarBuilder jarBuilder = new JarBuilder(jarPath);
                    jarBuilder.deleteFiles(deleteEntries);

                    VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);

                    NoticeInfo.info("Delete success !");

                }catch (Throwable e) {
                    NoticeInfo.error("Delete files err",e);
                }
            }
        });
    }

}
