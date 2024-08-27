package com.liubs.jareditor.bytestool.javassist;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.EditorTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.liubs.jareditor.editor.MyJarEditor;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * javassist面板
 * @author Liubsyy
 * @date 2024/8/27
 */
public class JavassistDialog extends DialogWrapper {

    private final Project project;
    private final VirtualFile virtualFile;
    private JavassistTool javassistTool;
    private Editor editor;
//    private EditorTextField editorTextField;


    public JavassistDialog(@Nullable Project project, VirtualFile virtualFile) {
        super(true);
        this.project = project;
        this.virtualFile = virtualFile;
        this.javassistTool = new JavassistTool(project,virtualFile);

        init();
        setTitle("Javassist Tool");
        pack(); //调整窗口大小以适应其子组件
        setModal(false);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

        JPanel mainPanel = new JPanel(new GridLayoutManager(7, 2));
        mainPanel.setPreferredSize(new Dimension(500, 500));

        String className = virtualFile.getName();
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile instanceof PsiClassOwner) {
            PsiClass[] classes = ((PsiClassOwner) psiFile).getClasses();
            for (PsiClass psiClass : classes) {
                className = psiClass.getQualifiedName();
                break;
            }
        }

        int line = 0;

        JLabel classNameLabel = new JLabel("Class");
        JLabel classNameValue = new JLabel(className);
        mainPanel.add(classNameLabel, new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(classNameValue, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));

        line++;

        JLabel actionLabel = new JLabel("Action");
        JRadioButton modifyRadio = new JRadioButton("Modify",true);
        JRadioButton addRadio = new JRadioButton("Add");
        JRadioButton deleteRadio = new JRadioButton("Delete");

        ButtonGroup group = new ButtonGroup();
        group.add(modifyRadio);
        group.add(addRadio);
        group.add(deleteRadio);

        JPanel radios = new JPanel();
        radios.add(modifyRadio);
        radios.add(Box.createHorizontalStrut(10));
        radios.add(addRadio);
        radios.add(Box.createHorizontalStrut(10));
        radios.add(deleteRadio);

        mainPanel.add(actionLabel, new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(radios, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));

        line++;

        JLabel targetLabel = new JLabel("Target");
        ComboBox<String> typeComboBox = new ComboBox<>();

        for(CtMethod ctMethod : javassistTool.getMethods()){
            typeComboBox.addItem(ctMethod.getName());
        }
        typeComboBox.addActionListener((e)->{
            List<PsiMethod> psiMethods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class)
                    .stream().filter(c -> typeComboBox.getSelectedItem().equals(c.getName())).collect(Collectors.toList());
            String text = psiMethods.get(0).getText();

            Document document = editor.getDocument();

            // 使用 WriteCommandAction 修改文件内容
            WriteCommandAction.runWriteCommandAction(project, () -> {
                document.setText(text);
            });

            // 提交文档更改
            PsiDocumentManager.getInstance(project).commitDocument(document);

        });


        mainPanel.add(targetLabel, new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(typeComboBox, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));


        line++;

        JLabel optItem = new JLabel("Operation");
        ComboBox<String> optItemBox = new ComboBox<>();
        optItemBox.addItem("setBody");
        optItemBox.addItem("insertBefore");
        optItemBox.addItem("insertAfter");



        mainPanel.add(optItem, new GridConstraints(line, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(optItemBox, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));


        line++;


        //Collection<PsiMethod> methods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class);


//
//        LightVirtualFile virtualFile = new LightVirtualFile("fileName",
//                JavaFileType.INSTANCE, "");
//        PsiFile psiFileLight = PsiManager.getInstance(project).findFile(virtualFile);

//        this.editor = EditorFactory.getInstance().createEditor(psiFileLight.getViewProvider().getDocument(), project);
//        this.editor = EditorFactory.getInstance().createEditor(psiFileLight.getViewProvider().getDocument(), project);


        this.editor = EditorFactory.getInstance().createEditor(EditorFactory.getInstance().createDocument(""), project);

        if(null != editor){
            if (editor instanceof EditorEx) {
                EditorEx editorEx = (EditorEx) editor;
                editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project, virtualFile));
                editorEx.setCaretVisible(true);
                editorEx.setEmbeddedIntoDialogWrapper(true);
            }
            mainPanel.add(editor.getComponent(), new GridConstraints(line, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                    GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                    GridConstraints.SIZEPOLICY_FIXED, new Dimension(500,300), null, null));

            line++;
        }

        /*
        // 设置编辑器的文件类型，例如 Java 或者 PlainText
//        FileType fileType = PlainTextFileType.INSTANCE;
        FileType fileType = JavaFileType.INSTANCE;

        // 创建一个虚拟文件作为编辑器的上下文（语法高亮需要）
        PsiFile psiFileLight = PsiFileFactory.getInstance(project)
                .createFileFromText("Dummy.java", fileType, "");


        // 创建 Editor
        editorTextField = new EditorTextField(psiFileLight.getViewProvider().getDocument(), project, fileType, false, false);
        editorTextField.setPreferredWidth(500);
        editor = editorTextField.getEditor();

        if (editor != null) {
            if (editor instanceof EditorEx) {
                EditorEx editorEx = (EditorEx) editor;
                editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(project,
                        psiFileLight.getVirtualFile()));
                editorEx.setHorizontalScrollbarVisible(true);
                editorEx.setVerticalScrollbarVisible(true);
            }

        }
        mainPanel.add(editorTextField, new GridConstraints(line, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, new Dimension(500,300), null, null));

        line++;
*/


        JButton saveBtn  = new JButton("Save");
        mainPanel.add(saveBtn, new GridConstraints(line, 1, 1, 1, GridConstraints.ANCHOR_EAST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));

        if(typeComboBox.getItemCount() > 0){
            typeComboBox.setSelectedIndex(0);
        }

        return mainPanel;
    }


    @Override
    protected Action [] createActions() {
        // Return an empty array to hide OK and Cancel buttons
        return new Action[0];
    }

}
