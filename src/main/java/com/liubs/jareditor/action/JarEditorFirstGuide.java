package com.liubs.jareditor.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.liubs.jareditor.firstguide.FirstGuideDialog;
import com.liubs.jareditor.persistent.GuideStorage;
import org.jetbrains.annotations.NotNull;

/**
 * @author Liubsyy
 * @date 2025/12/9
 */
public class JarEditorFirstGuide extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FirstGuideDialog dialog = new FirstGuideDialog();
        if(dialog.showAndGet()){
            GuideStorage.getInstance().setShowed(true);
        }
    }
}
