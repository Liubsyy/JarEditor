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
import com.liubs.jareditor.jarbuild.JarBuildResult;
import com.liubs.jareditor.jarbuild.JarBuilder;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.JarUtil;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Liubsyy
 * @date 2024/5/12
 */
public abstract class JavaEditorAddFile  extends AnAction {

    protected abstract String preInput( Project project,String entryPathFromJar);

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

        //当在一个文件a上右键新增文件b时，取a所在的文件夹进行新增b
        if(!"jar".equals(selectedFile.getExtension()) && !selectedFile.isDirectory()) {
            selectedFile = selectedFile.getParent();
            if(null == selectedFile) {
                NoticeInfo.warning("You need choose a folder in jar !");
                return;
            }
        }

        boolean isJarRoot = "jar".equals(selectedFile.getExtension());
        final String jarPath = isJarRoot ?
                selectedFile.getPath().replace(".jar!/",".jar") : MyPathUtil.getJarPathFromJar(selectedFile.getPath());
        final String entryPathFromJar = MyPathUtil.getEntryPathFromJar(selectedFile.getPath());
        if(null == jarPath) {
            NoticeInfo.warning("This operation only in JAR !!!");
            return;
        }

        String entryPath = preInput(project,entryPathFromJar);
        if(null == entryPath) {
            return;
        }


        //对话框输入完后提示弹不出来，不知道为什么
//        if(JarUtil.existEntry(jarPath,entryPath)) {
//            NoticeInfo.error("Already exists: %s",entryPath);
//            return;
//        }

        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Add file in JAR...", false) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {

                     if(JarUtil.existEntry(jarPath,entryPath)) {
                         NoticeInfo.error("Already exists: %s",entryPath);
                        return;
                     }

                    JarBuilder jarBuilder = new JarBuilder(jarPath);
                    JarBuildResult jarBuildResult = jarBuilder.addFile(entryPath);
                    if(!jarBuildResult.isSuccess()) {
                        NoticeInfo.error("Add file err: \n%s",jarBuildResult.getErr());
                        return;
                    }

                    VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);

                    /*
                    ApplicationManager.getApplication().invokeLater(() -> {
                        try{
                            VirtualFile localJarFile = LocalFileSystem.getInstance().findFileByPath(jarPath);
                            if (localJarFile == null) {
                                return;
                            }
                            localJarFile.refresh(false, true);
                            VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(localJarFile);
                            if (jarRoot == null) {
                                return;
                            }
                            VirtualFile openFile = VirtualFileManager.getInstance().findFileByUrl("jar://" + jarPath + "!/" + entryPath);
                            if (openFile != null) {
                                FileEditorManager.getInstance(project).openFile(openFile, true);
                            }
                        }catch (Throwable e) {
                            e.printStackTrace();
                        }

                    });*/

                }catch (Throwable e) {
                    NoticeInfo.error("Add file err",e);
                }
            }
        });
    }
}
