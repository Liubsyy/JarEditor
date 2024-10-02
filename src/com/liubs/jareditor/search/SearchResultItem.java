package com.liubs.jareditor.search;

import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.util.MyPathUtil;

/**
 * @author Liubsyy
 * @date 2024/7/17
 */
public class SearchResultItem {
    private VirtualFile file;
    private String entryPath;

    public SearchResultItem(VirtualFile file, String entryPath) {
        this.file = file;
        this.entryPath = entryPath;
    }

    public VirtualFile getFile() {
        return file;
    }

    /**
     * 这个toString就是搜索结果展示的行
     * @return
     */
    @Override
    public String toString() {
        return String.format("[%s] %s", MyPathUtil.getJarSingleName(file.getPath()),entryPath);
    }
}
