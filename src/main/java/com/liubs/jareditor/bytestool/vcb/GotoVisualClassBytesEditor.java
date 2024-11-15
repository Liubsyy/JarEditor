package com.liubs.jareditor.bytestool.vcb;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

/**
 * @author Liubsyy
 * @date 2024/11/15
 */
public class GotoVisualClassBytesEditor {


    public static void openVCBEditor(AnActionEvent e){
        AnAction openVCBEditor = ActionManager.getInstance().getAction("vcb.openClassEditor");
        if(null == openVCBEditor) {
            Messages.showMessageDialog( "You haven't install VisualClassBytes, please install VisualClassBytes from marketplace!!! ",
                    "Install VisualClassBytes",Messages.getWarningIcon());
            return;
        }

        openVCBEditor.actionPerformed(e);
    }


}
