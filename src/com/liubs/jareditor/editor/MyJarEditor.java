package com.liubs.jareditor.editor;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.PsiErrorElementUtil;
import com.liubs.jareditor.sdk.JavacToolProvider;
import com.liubs.jareditor.sdk.SDKManager;
import com.liubs.jareditor.template.TemplateManager;
import com.liubs.jareditor.util.ClassVersionUtil;
import com.liubs.jareditor.util.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * @author Liubsyy
 * @date 2024/5/8
 */
public class MyJarEditor extends UserDataHolderBase implements FileEditor {
    private final Project project;
    private final JPanel panel = new JPanel(new BorderLayout());
    private final VirtualFile file;
    private final Editor editor;

    private ComboBox<String> selectJDKComboBox;
    private ArrayList<String> javaHomes = new ArrayList<>();

    private ComboBox<String> selectVersionComboBox;

    private JarEditorCore jarEditorCore;


    private static String lastSelectItem = null;

    @Nullable
    @Override
    public VirtualFile getFile() {
        return file;
    }

    public MyJarEditor(Project project, VirtualFile file) {
        this.project = project;
        this.file = file;

        this.editor = createEditor();
        this.jarEditorCore = new JarEditorCore(project, file, editor);

        panel.add(editor.getComponent(), BorderLayout.CENTER);

        // Create buttons and their panel
        JButton compileButton = new JButton("Save/Compile");
        JButton rebuildJar = new JButton("Build Jar");
        JButton resetButton = new JButton("Reset");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        if("class".equals(file.getExtension())){
            //选中jdk
            selectJDKComboBox = new ComboBox<>();
            selectJDKComboBox.addItem("SDK Default");
            buttonPanel.add(new JLabel("JDK"));
            javaHomes.add("");

            Set<String> allItems = new HashSet<>();
            for(SDKManager.JDKItem jdkItem : SDKManager.getAllJDKs()){
                allItems.add(jdkItem.name);
                selectJDKComboBox.addItem(jdkItem.name);
                javaHomes.add(jdkItem.javaHome);
            }
            selectJDKComboBox.setSelectedItem("SDK Default");
            try{
                if(null != lastSelectItem && allItems.contains(lastSelectItem)) {
                    selectJDKComboBox.setSelectedItem(lastSelectItem);
                }
            }catch (Throwable ee) {
                selectJDKComboBox.setSelectedItem("SDK Default");
            }

            selectJDKComboBox.addActionListener((e)-> lastSelectItem = (String) selectJDKComboBox.getSelectedItem());
            buttonPanel.add(selectJDKComboBox);

            selectVersionComboBox = new ComboBox<>();
            String classVersion = ClassVersionUtil.detectClassVersion(file);
            int maxJdkVersion = -1;
            try{
                maxJdkVersion = JavacToolProvider.getMaxJdkVersion();
            }catch (Throwable eex){}

            if(maxJdkVersion < 0) {
                maxJdkVersion = 21;
            }
            for(int i=1;i<=maxJdkVersion;i++) {
                if(ClassVersionUtil.ELDEN_VERSIONS.containsKey(i)) {
                    selectVersionComboBox.addItem(ClassVersionUtil.ELDEN_VERSIONS.get(i));
                }else {
                    selectVersionComboBox.addItem(String.valueOf(i));
                }
            }
            selectVersionComboBox.setSelectedItem(classVersion);

            buttonPanel.add(new JLabel("Compiled Version"));
            buttonPanel.add(selectVersionComboBox);
        }

        buttonPanel.add(compileButton);
        buttonPanel.add(rebuildJar);
        buttonPanel.add(resetButton);

        // Add the button panel to the main panel
        panel.add(buttonPanel, BorderLayout.SOUTH);

        compileButton.addActionListener(e -> saveChanges());
        rebuildJar.addActionListener(e -> buildJar());
        resetButton.addActionListener(e -> cancelChanges());
    }

    private Editor createEditor(){
        String decompiledText = getDecompiledText(project, file);

        String fileExtension = file.getExtension();
        Editor editor = null;

        if(StringUtils.isEmpty(decompiledText)) {
            decompiledText = TemplateManager.getText(fileExtension, file.getPath());
        }

        if(null != fileExtension) {
            String fileName = file.getName();
            if("class".equals(fileExtension)) {
                fileExtension = JavaFileType.DEFAULT_EXTENSION;
                fileName = fileName.replace(".class", ".java");
            }

            try{
                FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(fileExtension);

                LightVirtualFile virtualFile = new LightVirtualFile(fileName,
                        fileType, decompiledText);
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

                if(null != psiFile && null != psiFile.getVirtualFile()) {
                    //editor = FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, psiFile.getVirtualFile()), true);
                    editor = EditorFactory.getInstance().createEditor(psiFile.getViewProvider().getDocument(), project);

                    // springboot项目暂时先禁用语法检测
                    if(file.getPath().contains("BOOT-INF")) {
                        DaemonCodeAnalyzer daemonCodeAnalyzer = DaemonCodeAnalyzer.getInstance(project);
                        daemonCodeAnalyzer.setHighlightingEnabled(psiFile, false);
                    }

                }

            }catch (Throwable e) {
                e.printStackTrace();
            }
        }


        //default editor
        if(null == editor) {
            Document document = EditorFactory.getInstance().createDocument(decompiledText);
            editor = EditorFactory.getInstance().createEditor(document, project);
        }

        if(null != editor){
            if (editor instanceof EditorEx) {
                EditorEx editorEx = (EditorEx) editor;
                editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, file));
                editorEx.setCaretVisible(true);
                editorEx.setEmbeddedIntoDialogWrapper(true);
            }
        }

        return editor;
    }

    private String getDecompiledText(Project project, VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null && !PsiErrorElementUtil.hasErrors(project, file)) {
            return psiFile.getText(); // 默认反编译器反编译结果
        }
        return "";
    }


    private void saveChanges() {
        if("class".equals(file.getExtension())){
            String javaHome = null;
            if(selectJDKComboBox.getSelectedIndex()>0) {
                javaHome = javaHomes.get(selectJDKComboBox.getSelectedIndex());
            }
            jarEditorCore.compileJavaCode(javaHome,(String) selectVersionComboBox.getSelectedItem());
        }else {
            jarEditorCore.saveResource();
        }
    }

    private void buildJar() {
        jarEditorCore.buildJar();
    }

    private void cancelChanges() {
        String decompiledText = getDecompiledText(project, file);
        Document document = editor.getDocument();

        // 使用 WriteCommandAction 修改文件内容
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.setText(decompiledText);
        });

        // 提交文档更改
        PsiDocumentManager.getInstance(project).commitDocument(document);
    }


    @Override
    public @NotNull JComponent getComponent() {
        return panel;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return editor.getContentComponent();
    }

    @Override
    public @NonNls @NotNull String getName() {
        return "Jar Editor";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {}

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return file.isValid();
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {}

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {}

    @Override
    public @Nullable FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {
        try{
            EditorFactory.getInstance().releaseEditor(editor);
        }catch (Throwable e) {}

    }


}
