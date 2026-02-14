package com.liubs.jareditor.action;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.ext.JarLikeSupports;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.JarUtil;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 展开嵌套jar结构
 * @author Liubsyy
 * @date 2024/10/13
 */
public class ExpandNestedJarAction extends AnAction {
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


    }

    public static void expandNestedJar(Project project,VirtualFile selectedFile){
        boolean isDirectory = selectedFile.isDirectory();
        if(!isDirectory && !JarLikeSupports.FILE_EXT.contains(selectedFile.getExtension())) {
            return;
        }

        String jarPath = MyPathUtil.getJarFullPath(selectedFile.getPath());
        String entryPath = MyPathUtil.getEntryPathFromJar(selectedFile.getPath());

        String nestedJarBasePath = MyPathUtil.getNestedJarPath(selectedFile.getPath());
        if(null == jarPath || null == nestedJarBasePath) {
            return;
        }
        List<String> destPaths = new ArrayList<>();
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                //nested jar
                if(entryName.endsWith(".jar")) {

                    boolean createDstJar;
                    if(null == entryPath) {
                        createDstJar = true;
                    }else if(isDirectory) {
                        createDstJar = entryName.startsWith(entryPath);
                    }else {
                        createDstJar = entryName.equals(entryPath);
                    }

                    if(createDstJar) {
                        Path destPath = Paths.get(nestedJarBasePath, entry.toString());
                        JarUtil.createFile(jarFile,entry,destPath);
                        destPaths.add(destPath.toString());
                    }
                }
            }

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        destPaths.forEach(c->{
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(c);
            if(null != virtualFile) {
                virtualFile.refresh(false, false);
            }
        });

        if(!destPaths.isEmpty()) {
            //这行代码会触发 JarTreeStructureProvider 重新刷新
            ProjectView.getInstance(project).refresh();
        }
    }

}
