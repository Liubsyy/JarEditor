package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import com.liubs.jareditor.structure.CompressionMethodDialog;
import com.liubs.jareditor.structure.NestedJar;
import com.liubs.jareditor.jarbuild.JarBuildResult;
import com.liubs.jareditor.jarbuild.JarBuilder;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * jar entry的压缩方式
 * @author Liubsyy
 * @date 2024/10/14
 */
public class CompressionMethod extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Project project = e.getProject();
        if(project == null) {
            NoticeInfo.warning("Please open a project");
            return;
        }
        if(null == selectedFile) {
            NoticeInfo.warning("No file selected");
            return;
        }
        if("jar".equals(selectedFile.getExtension()) && selectedFile.getPath().contains(NestedJar.KEY)) {
            String originalPath = PathUtil.getLocalPath(selectedFile.getPath()).replaceFirst(NestedJar.KEY,".jar!");
            selectedFile = VirtualFileManager.getInstance().findFileByUrl("jar://"+originalPath);
            if(null == selectedFile){
                return;
            }
        }

        String entryPathFromJar = MyPathUtil.getEntryPathFromJar(selectedFile.getPath());
        if(null == entryPathFromJar) {
            return;
        }
        final String jarPath = MyPathUtil.getJarPathFromJar(selectedFile.getPath());

        int method = -1;
        try(JarFile jarFile = new JarFile(jarPath)){
            ZipEntry entry = jarFile.getEntry(entryPathFromJar);
            method = entry.getMethod();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        CompressionMethodDialog dialog = new CompressionMethodDialog(entryPathFromJar,method);
        if(dialog.showAndGet()){
            int selectedMethod = dialog.getSelectedMethod();
            if(selectedMethod == method) {
                return;
            }

            ProgressManager.getInstance().run(new Task.Backgroundable(null, "Change compression method...", false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        JarBuilder jarBuilder = new JarBuilder(jarPath);
                        JarBuildResult jarBuildResult = jarBuilder.setCompressionMethod(entryPathFromJar,selectedMethod);
                        if(!jarBuildResult.isSuccess()) {
                            NoticeInfo.error("Change Compression err: \n%s",jarBuildResult.getErr());
                            return;
                        }
                        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
                        NoticeInfo.info("Change success, compression method=%s",selectedMethod == JarEntry.STORED ? "STORED" : "DEFLATED");
                    }catch (Throwable e) {
                        NoticeInfo.error("Add file err",e);
                    }
                }
            });
        }
    }



}
