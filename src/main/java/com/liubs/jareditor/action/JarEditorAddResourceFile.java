package com.liubs.jareditor.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.liubs.jareditor.util.StringUtils;

/**
 * 新增普通文件
 * @author Liubsyy
 * @date 2024/5/12
 */
public class JarEditorAddResourceFile  extends JarEditorAddFile {
    @Override
    protected String preInput(Project project, String entryPathFromJar) {
        String userInput = Messages.showInputDialog(
                project,
                "Enter name for new file:",
                "Create New File",
                Messages.getQuestionIcon()
        );
        if(StringUtils.isEmpty(userInput)) {
            return null;
        }
        if(null == entryPathFromJar) {
            return userInput;
        }
        return entryPathFromJar+"/"+userInput;
    }
}
