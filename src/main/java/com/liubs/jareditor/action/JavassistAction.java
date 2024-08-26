package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.bytestool.javassist.JavassistDialog;
import org.jetbrains.annotations.NotNull;

/**
 * javassist修改字节码工具入口
 * @author Liubsyy
 * @date 2024/8/27
 */
public class JavassistAction extends MyToolbarAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        VirtualFile selectedFile = super.currentEditorFile(e);
        if(null == selectedFile) {
            return;
        }

        JavassistDialog dialog = new JavassistDialog(e.getProject(),selectedFile);
        dialog.show();
    }
}
