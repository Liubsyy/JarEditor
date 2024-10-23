package com.liubs.jareditor.bytestool.asm.ui;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.liubs.jareditor.bytestool.asm.constant.AccessConstant;
import com.liubs.jareditor.bytestool.asm.tree.ClassInfoTreeNode;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * @author Liubsyy
 * @date 2024/10/23
 */
public class ClassInfoPanel extends JPanel implements IPanelRefresh<ClassInfoTreeNode> {
    private Project project;

    private JLabel minorVersion;
    private JLabel majorVersion;
    private JLabel superName;

    private JLabel access;

    private JLabel name;

    private JLabel sourceFile;
    private JLabel sourceDebug;


    public ClassInfoPanel(Project project) {
        this.project = project;

        setLayout(new BorderLayout());

        minorVersion = new JLabel();
        majorVersion = new JLabel();
        superName = new JLabel();
        access = new JLabel();
        name = new JLabel();
        sourceFile = new JLabel();
        sourceDebug = new JLabel();

        JPanel baseInfo = FormBuilder.createFormBuilder()
                .setVerticalGap(8)
                .addLabeledComponent("Minor Version : ", minorVersion)
                .addLabeledComponent("Major Version : ", majorVersion)
                .addLabeledComponent("Access : ", access)
                .addLabeledComponent("Name : ", name)
                .addLabeledComponent("Super : ", superName)
                .addLabeledComponent("Source File : ", sourceFile)
                .addLabeledComponent("Source Debug : ", sourceDebug)
                .getPanel();

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        baseInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(etchedBorder, "Class Info"),
                JBUI.Borders.empty(8)));

        this.add(baseInfo,BorderLayout.NORTH);
    }


    @Override
    public void refresh(ClassInfoTreeNode classInfoTreeNode) {
        ClassNode classNode = classInfoTreeNode.getClassNode();

        minorVersion.setText(String.valueOf(classNode.version>>16 & 0xFFFF));
        majorVersion.setText(String.valueOf(classNode.version & 0xFFFF));
        access.setText(String.format("0x%04x(%s)", classNode.access, String.join(" ", AccessConstant.getClassFlagNames(classNode.access))));
        name.setText(classNode.name);
        superName.setText(classNode.superName);
        sourceFile.setText(classNode.sourceFile);
        sourceDebug.setText(classNode.sourceDebug);
    }
}
