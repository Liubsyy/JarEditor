package com.liubs.jareditor.search;

import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.util.MyPathUtil;

/**
 * @author Liubsyy
 * @date 2024/7/17
 */
public class SearchResultItem {
    private VirtualFile jarFile;
    private String entryPath;

    public SearchResultItem(VirtualFile jarFile, String entryPath) {
        this.jarFile = jarFile;
        this.entryPath = entryPath;
    }

    public VirtualFile getJarFile() {
        return jarFile;
    }

    public void setJarFile(VirtualFile jarFile) {
        this.jarFile = jarFile;
    }

    public String getEntryPath() {
        return entryPath;
    }

    public void setEntryPath(String entryPath) {
        this.entryPath = entryPath;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", MyPathUtil.getJarSingleName(jarFile.getPath()),entryPath);
    }
}
