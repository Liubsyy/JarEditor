package com.liubs.jareditor.bytestool.asm.ui;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.liubs.jareditor.bytestool.asm.constant.AccessConstant;
import com.liubs.jareditor.bytestool.asm.tree.InnerClassTreeNode;
import org.objectweb.asm.tree.InnerClassNode;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * @author Liubsyy
 * @date 2024/10/23
 */
public class InnerClassPanel extends JPanel implements IPanelRefresh<InnerClassTreeNode> {

    private Project project;

    private JLabel access;
    private JLabel name;
    private JLabel innerName;
    private JLabel outerName;

    public InnerClassPanel(Project project) {
        this.project = project;

        setLayout(new BorderLayout());

        access = new JLabel();
        name = new JLabel();
        innerName = new JLabel();
        outerName = new JLabel();


        JPanel baseInfo = FormBuilder.createFormBuilder()
                .setVerticalGap(8)
                .addLabeledComponent("Access : ", access)
                .addLabeledComponent("Name : ", name)
                .addLabeledComponent("Inner Name : ", innerName)
                .addLabeledComponent("Outer Name : ", outerName)
                .getPanel();

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        baseInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(etchedBorder, "Inner Class"),
                JBUI.Borders.empty(8)));

        this.add(baseInfo,BorderLayout.NORTH);
    }

    @Override
    public void refresh(InnerClassTreeNode treeNode) {
        InnerClassNode innerClassNode = treeNode.getInnerClassNode();
        access.setText(String.format("0x%04x(%s)", innerClassNode.access, String.join(" ", AccessConstant.getClassFlagNames(innerClassNode.access))));
        name.setText(innerClassNode.name);
        innerName.setText(innerClassNode.innerName);
        outerName.setText(innerClassNode.outerName);
    }
}
