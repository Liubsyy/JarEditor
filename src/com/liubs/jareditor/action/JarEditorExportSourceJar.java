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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.liubs.jareditor.constant.PathConstant;
import com.liubs.jareditor.editor.MyJarEditor;
import com.liubs.jareditor.jarbuild.JarBuilder;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.MyPathUtil;
import com.liubs.jareditor.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * 导出源码source jar
 * @author Liubsyy
 * @date 2024/7/15
 */
public class JarEditorExportSourceJar  extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if(project == null) {
            NoticeInfo.warning("Please open a project");
            return;
        }

        //支持多选导出
        VirtualFile[] selectedFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if(null == selectedFiles || selectedFiles.length==0) {
            NoticeInfo.warning("No file selected");
            return;
        }

        Set<String> fullJarFiles = new HashSet<>();
        for(VirtualFile virtualFile : selectedFiles) {
            if(null != virtualFile) {
                String jarFullPath = MyPathUtil.getJarFullPath(virtualFile.getPath());
                if(null != jarFullPath) {
                    fullJarFiles.add(jarFullPath);
                }
            }
        }
        if(fullJarFiles.isEmpty()) {
            NoticeInfo.warning("No jar selected");
            return;
        }

        String userInput = Messages.showInputDialog(
                e.getProject(),
                String.format("You are exporting %d jar, enter the suffix of target source jar.\n Demo: if you input \"-source-export.jar\" of demo.jar ,\n you will get \"demo-source-export.jar\"",fullJarFiles.size()),
                "Export source jar",
                Messages.getQuestionIcon(),
                PathConstant.EXPORT_SOURCE_NAME_SUFFIX,
                null
        );

        if(StringUtils.isEmpty(userInput)) {
            return;
        }


        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Export source jar ...", true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {

                    if(!userInput.endsWith(".jar")) {
                        NoticeInfo.warning("You must input end with .jar");
                        return;
                    }


                    fullJarFiles.parallelStream().forEach(jarPath->{

                        JarBuilder jarBuilder = new JarBuilder(jarPath);
                        ApplicationManager.getApplication().runReadAction(() -> {
                            jarBuilder.decompileContent(jarPath.replace(".jar",userInput), (entry)->{
                                VirtualFile virtualJar = VirtualFileManager.getInstance().findFileByUrl("jar://" + jarPath + "!/"+entry);
                                if(null == virtualJar) {
                                    return null;
                                }else {
                                    String allText = MyJarEditor.getDecompiledText(project, virtualJar);
                                    return null == allText ? null : allText.getBytes(StandardCharsets.UTF_8);
                                }
                            });
                        });

                    });

                    NoticeInfo.info("Export source jar success !");
                }catch (Throwable e) {
                    NoticeInfo.error("Export source jar err",e);
                }
            }
        });

    }
}
