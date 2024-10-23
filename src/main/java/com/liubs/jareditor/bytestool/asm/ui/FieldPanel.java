package com.liubs.jareditor.bytestool.asm.ui;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.liubs.jareditor.bytestool.asm.constant.AccessConstant;
import com.liubs.jareditor.bytestool.asm.tree.FieldTreeNode;
import org.objectweb.asm.tree.FieldNode;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * @author Liubsyy
 * @date 2024/10/23
 */
public class FieldPanel extends JPanel implements IPanelRefresh<FieldTreeNode> {
    private Project project;

    //access
    private JLabel access;

    //name
    private JLabel name;

    //desc
    private JLabel desc;

    private JLabel value;



    public FieldPanel(Project project) {
        this.project = project;

        setLayout(new BorderLayout());

        access = new JLabel();
        name = new JLabel();
        desc = new JLabel();
        value = new JLabel();

        JPanel baseInfo = FormBuilder.createFormBuilder()
                .setVerticalGap(8)
                .addLabeledComponent("Access : ", access)
                .addLabeledComponent("Name : ", name)
                .addLabeledComponent("Desc : ", desc)
                .addLabeledComponent("Value : ", value)
                .getPanel();

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        baseInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(etchedBorder, "Field Info"),
                JBUI.Borders.empty(8)));

        this.add(baseInfo,BorderLayout.NORTH);
    }

    @Override
    public void refresh(FieldTreeNode fieldTreeNode) {
        FieldNode fieldNode = fieldTreeNode.getFieldNode();

        access.setText(String.format("0x%04x(%s)", fieldNode.access, String.join(" ", AccessConstant.getFieldsFlagNames(fieldNode.access))));
        name.setText(fieldNode.name);
        desc.setText(fieldNode.desc);

        value.setText(null == fieldNode.value ? null : String.valueOf(fieldNode.value));
    }
}
