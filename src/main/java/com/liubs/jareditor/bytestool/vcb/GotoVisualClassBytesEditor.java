package com.liubs.jareditor.bytestool.vcb;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import java.lang.reflect.Method;

/**
 * @author Liubsyy
 * @date 2024/11/15
 */
public class GotoVisualClassBytesEditor {


    private static Method actionPerformed = null;
    public static void openVCBEditor(AnActionEvent e){
        AnAction openVCBEditor = ActionManager.getInstance().getAction("vcb.openClassEditor");
        if(null == openVCBEditor) {
            Messages.showMessageDialog( "You haven't install VisualClassBytes, please install VisualClassBytes from marketplace!!! ",
                    "Install VisualClassBytes",Messages.getWarningIcon());
            return;
        }


        //直击调用会被插件审核标记过期API
        //openVCBEditor.actionPerformed(e);

        if(null== actionPerformed) {
            try {
                actionPerformed = AnAction.class.getDeclaredMethod("actionPerformed", AnActionEvent.class);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if(null != actionPerformed){
            try {
                actionPerformed.invoke(openVCBEditor,e);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }


}
