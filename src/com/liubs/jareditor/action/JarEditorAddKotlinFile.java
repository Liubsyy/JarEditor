package com.liubs.jareditor.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.liubs.jareditor.util.StringUtils;

/**
 * @author Liubsyy
 * @date 2024/6/2
 */
public class JarEditorAddKotlinFile extends JavaEditorAddFile {

    @Override
    protected String preInput(Project project, String entryPathFromJar) {
        String userInput = Messages.showInputDialog(
                project,
                "Enter name for kotlin class:",
                "Create Kotlin Class",
                Messages.getQuestionIcon()
        );
        if(StringUtils.isEmpty(userInput)) {
            return null;
        }
        if(null == entryPathFromJar){
            return userInput+".kt";
        }
        return entryPathFromJar+"/"+userInput+".kt";
    }

}
