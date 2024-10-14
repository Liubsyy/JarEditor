package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.liubs.jareditor.jarbuild.JarBuildResult;
import com.liubs.jareditor.jarbuild.JarRefactor;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.MyPathUtil;
import com.liubs.jareditor.util.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 重构package或者Class
 * 不仅重命名包和类本身，引用修改类的地方也需要修改
 * @author Liubsyy
 * @date 2024/9/12
 */
public class JarEditorRefactorClass  extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if(e.getProject() == null) {
            NoticeInfo.warning("Please open a project");
            return;
        }
        if(null == selectedFile) {
            NoticeInfo.warning("No file selected");
            return;
        }

        final String jarPath =  MyPathUtil.getJarPathFromJar(selectedFile.getPath());
        if(null == jarPath) {
            NoticeInfo.warning("This operation only in JAR !!!");
            return;
        }

        String entryPathFromJar = MyPathUtil.getEntryPathFromJar(selectedFile.getPath());

        final boolean isPackage = selectedFile.isDirectory();

        if(isPackage) {
            final String oldName = entryPathFromJar+"/";
            String userInput = Messages.showInputDialog(
                    e.getProject(),
                    "Enter new name:",
                    "Refactor Package",
                    Messages.getQuestionIcon(),
                    entryPathFromJar.replace("/","."),
                    null
            );
            if(StringUtils.isEmpty(userInput)) {
                return;
            }
            String newName = userInput.replace(".","/")+"/";

            ProgressManager.getInstance().run(new Task.Backgroundable(null, "Refactor package in JAR...", false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        JarRefactor jarRefactor = new JarRefactor(jarPath);
                        JarBuildResult jarBuildResult = jarRefactor.refactorPackage(oldName, newName);
                        if(!jarBuildResult.isSuccess()) {
                            NoticeInfo.error("Refactor package err: \n%s",jarBuildResult.getErr());
                            return;
                        }

                        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
                        NoticeInfo.info("Refactor package success !");

                    }catch (Throwable e) {
                        NoticeInfo.error("Refactor package err",e.getMessage());
                    }
                }
            });
        }else {

            String simpleName = selectedFile.getName();
            if(StringUtils.isNotEmpty(selectedFile.getExtension())){
                int extLength = selectedFile.getExtension().length();
                entryPathFromJar = entryPathFromJar.substring(0,entryPathFromJar.length()-extLength-1);
                simpleName = simpleName.substring(0,simpleName.length()-extLength-1);
            }

            final String oldName = entryPathFromJar;
            String userInput = Messages.showInputDialog(
                    e.getProject(),
                    "Enter new name:",
                    "Refactor Class",
                    Messages.getQuestionIcon(),
                    simpleName,
                    null
            );
            if(StringUtils.isEmpty(userInput)) {
                return;
            }

            String newName = MyPathUtil.getEntryPathFromJar(selectedFile.getParent().getPath())+"/"+userInput;
            ProgressManager.getInstance().run(new Task.Backgroundable(null, "Refactor class in JAR...", false) {
                @Override
                public void run(@NotNull ProgressIndicator progressIndicator) {
                    try {
                        JarRefactor jarRefactor = new JarRefactor(jarPath);
                        JarBuildResult jarBuildResult = jarRefactor.refactorClass(oldName, newName);
                        if(!jarBuildResult.isSuccess()) {
                            NoticeInfo.error("Refactor class err: \n%s",jarBuildResult.getErr());
                            return;
                        }

                        VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
                        NoticeInfo.info("Refactor class success !");

                    }catch (Throwable e) {
                        NoticeInfo.error("Refactor class err",e.getMessage());
                    }
                }
            });
        }
    }
}
