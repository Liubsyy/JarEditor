package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.bytestool.vcb.GotoVisualClassBytesEditor;
import org.jetbrains.annotations.NotNull;

/**
 * @author Liubsyy
 * @date 2024/11/15
 */
public class VcbEditorAction extends MyToolbarAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = super.currentEditorFile(e);
        if(null == selectedFile) {
            return;
        }
        GotoVisualClassBytesEditor.openVCBEditor(e);
    }
}
