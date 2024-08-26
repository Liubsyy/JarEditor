package com.liubs.jareditor.bytestool.javassist;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * javassist面板
 * @author Liubsyy
 * @date 2024/8/27
 */
public class JavassistDialog extends DialogWrapper {

    private final Project project;
    private final VirtualFile virtualFile;


    public JavassistDialog(@Nullable Project project, VirtualFile virtualFile) {
        super(true);
        this.project = project;
        this.virtualFile = virtualFile;

        init();
        setTitle("Javassist Tool");
        pack(); //调整窗口大小以适应其子组件
        setModal(false);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {

        JPanel mainPanel = new JPanel(new GridLayoutManager(6, 2));
        mainPanel.setPreferredSize(new Dimension(500, 500));


        JLabel classNameLabel = new JLabel("Class Name");
        JLabel classNameValue = new JLabel(virtualFile.getName());
        mainPanel.add(classNameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(classNameValue, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));

        JLabel type = new JLabel("Type");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.addItem("Field");
        typeComboBox.addItem("Method");

        mainPanel.add(type, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(typeComboBox, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));


        JLabel optItem = new JLabel("Operate item");
        ComboBox<String> optItemBox = new ComboBox<>();
        optItemBox.addItem("Add method");
        optItemBox.addItem("Add field");
        optItemBox.addItem("Delete field");
        optItemBox.addItem("Set body");

        mainPanel.add(optItem, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(optItemBox, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));


        JLabel operation = new JLabel("Operation");
        ComboBox<String> operationBox = new ComboBox<>();
        operationBox.addItem("protected @Nullable JComponent createCenterPanel();");
        operationBox.addItem("private int a;");
        operationBox.addItem("private int b;");

        mainPanel.add(operation, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));
        mainPanel.add(operationBox, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null));


        LightVirtualFile virtualFile = new LightVirtualFile("fileName",
                JavaFileType.INSTANCE, "decompiledText");
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

        Editor editor = EditorFactory.getInstance().createEditor(psiFile.getViewProvider().getDocument(), project);

        mainPanel.add(editor.getComponent(), new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, new Dimension(500,300), null, null));

        JButton saveBtn  = new JButton("Save");
        mainPanel.add(saveBtn, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_EAST,
                GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED, null, null, null));



        return mainPanel;
    }


    @Override
    protected Action [] createActions() {
        // Return an empty array to hide OK and Cancel buttons
        return new Action[0];
    }

}
