package com.liubs.jareditor.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.liubs.jareditor.util.StringUtils;

/**
 * @author Liubsyy
 * @date 2024/5/14
 */
public class JarEditorAddDictionary  extends JavaEditorAddFile {
    @Override
    protected String preInput(Project project, String entryPathFromJar) {
        String userInput = Messages.showInputDialog(
                project,
                "Enter name for new class:",
                "Create New Class",
                Messages.getQuestionIcon()
        );
        if(StringUtils.isEmpty(userInput)) {
            return null;
        }

        //文件夹以/结尾
        return entryPathFromJar+"/"+userInput+"/";
    }
}
