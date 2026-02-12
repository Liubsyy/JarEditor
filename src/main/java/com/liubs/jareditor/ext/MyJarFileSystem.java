package com.liubs.jareditor.ext;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.jar.JarFileSystemImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author Liubsyy
 * @date 2026/2/12
 */
public class MyJarFileSystem extends JarFileSystemImpl {

    public static MyJarFileSystem getInstance() {
        return ApplicationManager.getApplication().getService(MyJarFileSystem.class);
    }

    @Override
    protected boolean isCorrectFileType(@NotNull VirtualFile local) {
        if(null != local.getExtension() && local.getExtension().equalsIgnoreCase("aar")) {
            return true;
        }
        return super.isCorrectFileType(local);
    }
}
