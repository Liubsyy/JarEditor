package com.liubs.jareditor.filetree;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/10/13
 */
public class NestedJarChangedListener implements BulkFileListener {

    private final Project project;
    public NestedJarChangedListener(Project project) {
        this.project = project;
    }


    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {

        NestedJarHolder nestedJarHolder = NestedJarHolder.getInstance(project);
        if(null == nestedJarHolder) {
            return;
        }
        boolean refreshFileTree = false;
        for(VFileEvent vFileEvent : events) {

            //如果删除了嵌套jar的目标路径，重新刷新一下文件树
            if(vFileEvent instanceof VFileDeleteEvent) {
                String localPath = PathUtil.getLocalPath(vFileEvent.getPath());
                if(nestedJarHolder.containsPath(localPath)){
                    nestedJarHolder.removeExpandPath(localPath);
                    refreshFileTree = true;
                    break;
                }
            }
        }
        if(refreshFileTree){
            //这行代码会触发 JarTreeStructureProvider 重新刷新
            ProjectView.getInstance(project).refresh();
        }
    }
}
