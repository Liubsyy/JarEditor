package com.liubs.jareditor.editor;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.util.PsiErrorElementUtil;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;

/**
 * @author Liubsyy
 * @date 2024/6/21
 */
public class SourceJarResolver {
    private final Project project;
    private final VirtualFile file;
    private final Editor editor;
    private final boolean isSourceJar;

    //展示反编译代码还是source jar中的代码
    private EditorNotificationPanel decompiledOrSourceTextPanel;
    private HyperlinkLabel hyperlinkLabel;

    //是否来自反编译的代码
    private boolean isFromDecompiled = true;

    public SourceJarResolver(Project project, VirtualFile file,Editor editor) {
        this.project = project;
        this.file = file;
        this.editor = editor;
        this.isSourceJar = MyPathUtil.isSourceJar(file.getPath());
    }

    public boolean isSourceJar(){
        return isSourceJar;
    }

    public EditorNotificationPanel createSourceJarNotification(){
        EditorNotificationPanel sourceJarNotice = new EditorNotificationPanel();
        sourceJarNotice.setText("You are opening a source jar, not class jar");
        sourceJarNotice.createActionLabel("Click here to open class jar", new EditorNotificationPanel.ActionHandler() {
            @Override
            public void handlePanelActionClick(@NotNull EditorNotificationPanel editorNotificationPanel, @NotNull HyperlinkEvent hyperlinkEvent) {

                String replaceUrl = getClassFileUrl();
                if(null == replaceUrl) {
                    return;
                }
                VirtualFile openFile = VirtualFileManager.getInstance().findFileByUrl(replaceUrl);
                if (openFile != null) {
                    FileEditorManager.getInstance(project).openFile(openFile, true);
                }
            }

            @Override
            public void handleQuickFixClick(@NotNull Editor editor, @NotNull PsiFile psiFile) {}
        },false);

        return sourceJarNotice;
    }

    public EditorNotificationPanel getDecompiledOrSourceTextPanel(){
        if(null == decompiledOrSourceTextPanel) {
            decompiledOrSourceTextPanel = new EditorNotificationPanel();
            hyperlinkLabel = decompiledOrSourceTextPanel.createActionLabel("Click here", new EditorNotificationPanel.ActionHandler() {
                @Override
                public void handlePanelActionClick(@NotNull EditorNotificationPanel editorNotificationPanel, @NotNull HyperlinkEvent hyperlinkEvent) {
                    isFromDecompiled = !isFromDecompiled;

                    updateEditor();
                    updateDecompiledOrSourceTextPanel();
                }

                @Override
                public void handleQuickFixClick(@NotNull Editor editor, @NotNull PsiFile psiFile) {}
            }, false);

            updateDecompiledOrSourceTextPanel();
        }

        return decompiledOrSourceTextPanel;
    }


    public void updateDecompiledOrSourceTextPanel(){
        if(isFromDecompiled) {
            decompiledOrSourceTextPanel.setText("The code is decompiled from the .class file");
            hyperlinkLabel.setHyperlinkText("Import from source jar");
        }else {
            decompiledOrSourceTextPanel.setText("The code is from the source jar");
            hyperlinkLabel.setHyperlinkText("Show code from decompiled .class file");
            decompiledOrSourceTextPanel.setVisible(false);
        }
    }


    public void updateEditor(){
        String text = null;

        if(isFromDecompiled){
            text = MyJarEditor.getDecompiledText(project,file);
        }else {
            String sourceFileUrl = getSourceFileUrl();
            if(null != sourceFileUrl){
                VirtualFile sourceFile = VirtualFileManager.getInstance().findFileByUrl(sourceFileUrl);
                if(null != sourceFile) {
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(sourceFile);
                    if (psiFile != null && !PsiErrorElementUtil.hasErrors(project, sourceFile)) {
                        text = psiFile.getText();
                    }
                }
            }
        }

        Document document = editor.getDocument();

        // 使用 WriteCommandAction 修改文件内容
        String finalText = text;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.setText(finalText);
        });

        // 提交文档更改
        PsiDocumentManager.getInstance(project).commitDocument(document);
    }


    private String getClassFileUrl(){
        if(isSourceJar) {
            return file.getUrl().replace("-sources.jar!", ".jar!")
                    .replace(".java", ".class");
        }
        return null;
    }

    private String getSourceFileUrl(){
        if("class".equals(file.getExtension())){
            return file.getUrl().replace(".jar!", "-sources.jar!")
                    .replace(".class", ".java");
        }

        return null;
    }


    public boolean isClassJarAndHasSourceFile() {
        if("class".equals(file.getExtension())){
            String replaceUrl = file.getUrl().replace(".jar!", "-sources.jar!")
                    .replace(".class", ".java");
            VirtualFile sourceFile = VirtualFileManager.getInstance().findFileByUrl(replaceUrl);
            return null != sourceFile;
        }
        return false;
    }

    public static VirtualFile findSourceJar(VirtualFile classJar) {
        String replaceUrl = classJar.getUrl().replace(".jar!", "-sources.jar!");
        VirtualFile sourceFile = VirtualFileManager.getInstance().findFileByUrl(replaceUrl);
        return sourceFile;
    }

}
