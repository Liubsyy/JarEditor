package com.liubs.jareditor.structure;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.libraries.LibraryTablesRegistrar;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.action.ExpandNestedJarAction;
import com.liubs.jareditor.ext.JarLikeSupports;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * 由于未添加到Libraries的jar包无法展开，很多新手不会打开jar包内容，这里增加双击展开jar的人性化操作
 * @author Liubsyy
 * @date 2026/2/14
 */
public class JarDoubleClickProvider implements FileEditorProvider {

    private static Set<String> ACCEPTS = new HashSet<>();
    static {
        ACCEPTS.add(JarLikeSupports.JAR);
        ACCEPTS.add(JarLikeSupports.WAR);
        ACCEPTS.add(JarLikeSupports.EAR);
    }

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return null != file.getExtension() && ACCEPTS.contains(file.getExtension());
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project,
                            @NotNull VirtualFile file) {

        try{
            // ✅ 每次双击 jar 文件都会执行这里
            System.out.println("Double clicked jar: " + file.getPath());

            handleOpenJarView(project,file);

            ApplicationManager.getApplication().invokeLater(() ->{
                try{
                    FileEditorManager.getInstance(project).closeFile(file);
                }catch (Throwable e){}
            });
        }catch (Throwable e){}
        return new EmptyFileEditor(file);
    }

    private void handleOpenJarView(@NotNull Project project, @NotNull VirtualFile file) {
        if(JarTreeStructureProvider.isNestedJar(file)) {
            //嵌套jar直接触发展开操作
            ExpandNestedJarAction.expandNestedJar(project,file);
        }else {
            //普通jar添加到依赖Libraries
            String jarSimpleName = MyPathUtil.getSingleFileName(file.getPath());
            int i = jarSimpleName.lastIndexOf(".");
            if(i>0) {
                jarSimpleName = jarSimpleName.substring(0,i);
            }
            addJarAsProjectLibraryAndDependency(project, Paths.get(file.getPath()),jarSimpleName);
        }
    }

    /**
     * @param project 当前项目
     * @param jarPath jar 的磁盘路径（例如 Path.of("/path/to/a.jar")）
     * @param libraryName 在 Libraries 里显示的名字
     */
    public static void addJarAsProjectLibraryAndDependency(@NotNull Project project,
                                                           @NotNull Path jarPath,
                                                           @NotNull String libraryName) {
        WriteAction.run(() -> {
            try{
                // 1) 找到 Project-level LibraryTable
                LibraryTable libraryTable =
                        LibraryTablesRegistrar.getInstance().getLibraryTable(project); // Project Libraries :contentReference[oaicite:1]{index=1}

                // 2) 如果已存在同名库，复用；否则创建
                Library library = libraryTable.getLibraryByName(libraryName);
                if (library == null) {
                    LibraryTable.ModifiableModel tableModel = libraryTable.getModifiableModel();
                    library = tableModel.createLibrary(libraryName);               // 创建库 :contentReference[oaicite:2]{index=2}
                    tableModel.commit();
                }

                // 3) 把 jar 加到 library roots（CLASSES）
                VirtualFile jarLocal = LocalFileSystem.getInstance()
                        .refreshAndFindFileByPath(jarPath.toString().replace('\\', '/'));
                if (jarLocal == null) {
                    throw new IllegalArgumentException("Jar not found in VFS: " + jarPath);
                }

                VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(jarLocal);
                if (jarRoot == null) {
                    throw new IllegalStateException("Not a valid jar: " + jarPath);
                }

                Library.ModifiableModel libModel = library.getModifiableModel();
                libModel.addRoot(jarRoot, OrderRootType.CLASSES);                // addRoot/commit :contentReference[oaicite:3]{index=3}
                libModel.commit();

                // 4) 把这个 library 加到 module dependencies
//            ModuleRootModificationUtil.addDependency(module, library);       // 推荐的加依赖方式 :contentReference[oaicite:4]{index=4}
                for(com.intellij.openapi.module.Module module : ModuleManager.getInstance(project).getModules()){
                    ModuleRootModificationUtil.addDependency(module, library);
                }
            }catch (Throwable e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public @NotNull String getEditorTypeId() {
        return "jar-double-click-editor";
    }

    @Override
    public @NotNull
    FileEditorPolicy getPolicy() { return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR; }

    // 简单空编辑器
    static class EmptyFileEditor implements FileEditor {
        private final JPanel panel = new JPanel();
        private VirtualFile file;
        public EmptyFileEditor(VirtualFile file) {
            this.file = file;
        }
        @Override public @NotNull JComponent getComponent() { return panel; }
        @Override public JComponent getPreferredFocusedComponent() { return null; }
        @Override public @NotNull String getName() { return "JarPrint"; }
        @Override public void setState(@NotNull FileEditorState state) {}
        @Override public boolean isModified() { return false; }
        @Override public boolean isValid() { return true; }
        @Override public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {}
        @Override public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {}
        @Override
        public @Nullable FileEditorLocation getCurrentLocation() { return null; }
        @Override public void dispose() {}
        @Override
        public <T>  T getUserData(@NotNull Key<T> key) { return null; }
        @Override
        public <T> void putUserData(@NotNull Key<T> key, @Nullable T t) {}

        @Override
        public @Nullable VirtualFile getFile() { return file; }
    }
}