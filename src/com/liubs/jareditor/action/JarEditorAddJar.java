package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.jarbuild.JarBuilder;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.template.TemplateManager;
import com.liubs.jareditor.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * 新增一个jar文件
 * @author Liubsyy
 * @date 2024/11/25
 */
public class JarEditorAddJar extends AnAction {

    private JarEditorAddJarInJar jarEditorAddJarInJar = new JarEditorAddJarInJar();

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if(null == selectedFile) {
            NoticeInfo.warning("No file selected");
            return;
        }

        //jar内新增jar
        if("jar".equals(selectedFile.getExtension()) || selectedFile.getPath().contains(".jar!/")) {
            jarEditorAddJarInJar.actionPerformed(e);
            return;
        }

        Project project = e.getProject();
        if(project == null) {
            NoticeInfo.warning("Please open a project");
            return;
        }

        String dir = selectedFile.isDirectory() ? selectedFile.getPath() : selectedFile.getParent().getPath();

        String userInput = Messages.showInputDialog(
                project,
                "Enter JAR name:",
                "Create New JAR",
                Messages.getQuestionIcon(),
                ".jar",
                null
        );
        if(StringUtils.isEmpty(userInput)) {
            return;
        }

        String jarPath = dir+"/"+userInput;

        ProgressManager.getInstance().run(new Task.Backgroundable(null, "New JAR ...", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    File file = new File(jarPath);

                    if(file.exists()) {
                        NoticeInfo.error("Already exists: %s",jarPath);
                        return;
                    }
                    file.getParentFile().mkdirs();

                    try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(file))) {

                        JarEntry META_INF = JarBuilder.createStoredEntry("META-INF/", "".getBytes());
                        jarOutputStream.putNextEntry(META_INF);
                        jarOutputStream.closeEntry();

                        byte[] bytes = TemplateManager.getText("MF", jarPath).getBytes(StandardCharsets.UTF_8);
                        JarEntry MANIFEST_MF = JarBuilder.createStoredEntry("META-INF/MANIFEST.MF",bytes);
                        jarOutputStream.putNextEntry(MANIFEST_MF);
                        jarOutputStream.write(bytes);
                        jarOutputStream.closeEntry();
                    }

                    ApplicationManager.getApplication().invokeLater(() -> {
                        try{
                            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(jarPath);
                            if(null != virtualFile) {
                                virtualFile.refresh(false, false);
                            }
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    });

                }catch (Throwable e) {
                    NoticeInfo.error("New JAR err",e);
                }
            }
        });

    }




}
