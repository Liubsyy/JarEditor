package com.liubs.jareditor.action;

import com.intellij.openapi.project.Project;

/**
 * @author Liubsyy
 * @date 2024/10/15
 */
public class JarEditorAddManifest extends JavaEditorAddFile {

    @Override
    protected String preInput(Project project, String entryPathFromJar) {
        if(null == entryPathFromJar) {
            return "META-INF/MANIFEST.MF";
        }
        return entryPathFromJar+"/MANIFEST.MF";
    }
}
