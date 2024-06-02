package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
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

        Set<String> deleteEntries = new HashSet<>();
        for (VirtualFile file : selectedFiles) {
            if(!file.getPath().contains(".jar!/")) {
                NoticeInfo.warning("Ony files in JAR can be copy !!!");
                return;
            }
            String entryPathFromJar = MyPathUtil.getEntryPathFromJar(file.getPath());
            if(null != entryPathFromJar) {
                if(file.isDirectory()) {
                    //删除文件夹 /dir 导致/dir为前缀的文件夹(非子文件夹) 也删除的问题
                    deleteEntries.add(entryPathFromJar.replace("\\", "/")+"/");
                }else {
                    deleteEntries.add(entryPathFromJar.replace("\\", "/"));
                }

            }
        }

        if(deleteEntries.isEmpty()) {
            NoticeInfo.warning("Please select any file to delete!!");
            return;
        }
    }
}
