package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.liubs.jareditor.clipboard.ClipboardToFile;
import com.liubs.jareditor.clipboard.CopyResult;
import com.liubs.jareditor.jarbuild.JarBuildResult;
import com.liubs.jareditor.jarbuild.JarBuilder;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;


/**
 * @author Liubsyy
 * @date 2024/6/2
 */
public class JarEditorPasteFile extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Project project = e.getProject();
        if(project == null) {
            NoticeInfo.warning("Please open a project");
            return;
        }

        pasteFile(selectedFile);
    }

    public static void pasteFile(VirtualFile selectedFile){

        if(null == selectedFile) {
            NoticeInfo.warning("No file selected");
            return;
        }

        if(!selectedFile.isDirectory()) {
            selectedFile = selectedFile.getParent();
            if(null == selectedFile) {
                NoticeInfo.warning("You need choose a folder in jar !");
                return;
            }
        }

        boolean isJarRoot = "jar".equals(selectedFile.getExtension());
        final String jarPath = isJarRoot ?
                selectedFile.getPath().replace(".jar!/",".jar") : MyPathUtil.getJarPathFromJar(selectedFile.getPath());
        final String filePath = selectedFile.getPath();
        final String entryPathFromJar = MyPathUtil.getEntryPathFromJar(selectedFile.getPath());
        if(null == jarPath) {
            NoticeInfo.warning("This operation only in JAR !!!");
            return;
        }


        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Paste files from clipboard...", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                ClipboardToFile clipboardToFile = null;
                try {
                    String clipboard_to_fileDir = MyPathUtil.getCLIPBOARD_TO_FILE(filePath);

                    clipboardToFile = new ClipboardToFile(null == entryPathFromJar ?
                            clipboard_to_fileDir
                            :
                            clipboard_to_fileDir+"/"+entryPathFromJar);
                    CopyResult copyResult = clipboardToFile.copyFilesFromClipboard();

                    if(!copyResult.isSuccess()) {
                        NoticeInfo.error(copyResult.getError());
                        return;
                    }

                    JarBuilder jarBuilder = new JarBuilder(clipboard_to_fileDir,jarPath);
                    JarBuildResult jarBuildResult = jarBuilder.writeJar(false);
                    if(!jarBuildResult.isSuccess()) {
                        NoticeInfo.error("Build jar err: \n%s",jarBuildResult.getErr());
                        return;
                    }

                    VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);

                    NoticeInfo.info("Paste successfully!");

                }catch (Throwable e) {
                    NoticeInfo.error("Paste files from clipboard err",e.getMessage());
                }finally {
                    if(null != clipboardToFile) {
                        clipboardToFile.deleteTargetDir();
                    }
                }
            }
        });
    }

}
