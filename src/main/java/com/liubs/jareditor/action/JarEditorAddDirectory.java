package com.liubs.jareditor.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.liubs.jareditor.util.StringUtils;

/**
 * 新增文件夹
 * @author Liubsyy
 * @date 2024/5/14
 */
public class JarEditorAddDirectory extends JarEditorAddFile {
    @Override
    protected String preInput(Project project, String entryPathFromJar) {
        String userInput = Messages.showInputDialog(
                project,
                "Enter name for new directory:",
                "Create New Directory",
                Messages.getQuestionIcon()
        );
        if(StringUtils.isEmpty(userInput)) {
            return null;
        }
        if(null == entryPathFromJar) {
            return userInput+"/";
        }

        //文件夹以/结尾
        return entryPathFromJar+"/"+userInput+"/";
    }
}
