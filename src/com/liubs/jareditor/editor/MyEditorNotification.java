package com.liubs.jareditor.editor;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.HyperlinkEvent;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;


/**
 * @author Liubsyy
 * @date 2024/6/20
 */
public class MyEditorNotification extends EditorNotifications.Provider<EditorNotificationPanel> {

    //防重复创建
//    private Set<FileEditor> createdNotifications = Collections.newSetFromMap(new WeakHashMap<>());

    @NotNull
    @Override
    public Key<EditorNotificationPanel> getKey() {
        return Key.create("liubsyy.jar.editor.notification");
    }

    @Nullable
    @Override
    public EditorNotificationPanel createNotificationPanel(@NotNull VirtualFile file, @NotNull FileEditor fileEditor, @NotNull Project project) {
        if (MyPathUtil.isSourceJar(file.getPath()) && !(fileEditor instanceof MyJarEditor) ) {
//            if(createdNotifications.contains(fileEditor)) {
//                return null;
//            }
//            createdNotifications.add(fileEditor);
            return createNoticePanel0(project,file);
        }
        return null;
    }


    public static EditorNotificationPanel createNoticePanel0(@NotNull Project project, @NotNull VirtualFile file){
        EditorNotificationPanel sourceJarNotice = new EditorNotificationPanel();
        sourceJarNotice.setText("You are opening a source jar, not class jar");
        sourceJarNotice.createActionLabel("Click here to open class jar", new EditorNotificationPanel.ActionHandler() {
            @Override
            public void handlePanelActionClick(@NotNull EditorNotificationPanel editorNotificationPanel, @NotNull HyperlinkEvent hyperlinkEvent) {

                String replaceUrl = file.getUrl().replace("-sources.jar!", ".jar!")
                        .replace(".java", ".class");
                VirtualFile openFile = VirtualFileManager.getInstance().findFileByUrl(replaceUrl);
                if (openFile != null) {
                    FileEditorManager.getInstance(project).openFile(openFile, true);
                }
            }

            @Override
            public void handleQuickFixClick(@NotNull Editor editor, @NotNull PsiFile psiFile) {
            }
        },false);
        return sourceJarNotice;
    }


}
