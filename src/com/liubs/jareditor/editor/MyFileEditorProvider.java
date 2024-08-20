package com.liubs.jareditor.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Liubsyy
 * @date 2024/5/8
 */
public class MyFileEditorProvider implements FileEditorProvider {
    public static final String EDITOR_TYPE_ID = "liubsyy-jar-editor";

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        // 只扩展jar内文件
        //return "class".equalsIgnoreCase(file.getExtension())
        return file.getPath().contains(".jar!/");
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        try{
            return new MyJarEditor(project, file);
        }catch (Throwable ex) {

            /**
             * 重新打开IDEA项目时，可能会打开上次的jar(如果上次关闭项目时打开了jar文件的话)
             * 而打开项目初始化IDEA项目的线程不一定是EDT线程会有报错，虽然不影响使用，但是还是兼容一下
             *
             * === 尽管大部分IDEA版本甚至最新版本都没有这个问题，也不影响使用，这里还是做一个容错 ===
             * 目前发现IDEA2023.2有这个问题
             */
            if(!ApplicationManager.getApplication().isDispatchThread()) {
                CompletableFuture<MyJarEditor> editorFuture = new CompletableFuture<>();

                // 在EDT线程中创建Editor，并在完成后将结果设置到editorFuture
                ApplicationManager.getApplication().invokeLater(() -> {
                    try{
                        MyJarEditor myJarEditor = new MyJarEditor(project, file);
                        editorFuture.complete(myJarEditor);
                    }catch (Throwable e1){
                        e1.printStackTrace();
                    }
                });
                try {
                    return editorFuture.get(10, TimeUnit.SECONDS);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
            throw ex;
        }
    }


    @NotNull
    @Override
    public String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }



}
