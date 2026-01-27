package com.liubs.jareditor.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.liubs.jareditor.util.StringUtils;

/**
 * New Java file
 * @author Liubsyy
 * @date 2024/5/12
 */
public class JarEditorAddJavaFile extends JarEditorAddFile {

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

        //java package
        userInput = userInput.replace(".", "/");

        if(null == entryPathFromJar){
            return userInput+".class";
        }
        return entryPathFromJar+"/"+userInput+".class";
    }

}
