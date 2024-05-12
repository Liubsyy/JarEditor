package com.liubs.jareditor.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

/**
 * New Java file
 * @author Liubsyy
 * @date 2024/5/12
 */
public class JarEditorAddJavaFile  extends JavaEditorAddFile {

    @Override
    protected String preInput(Project project, String entryPathFromJar) {
        String userInput = Messages.showInputDialog(
                project,
                "Enter name for new class:",
                "Create New Class",
                Messages.getQuestionIcon()
        );
        return entryPathFromJar+"/"+userInput+".class";
    }

}
