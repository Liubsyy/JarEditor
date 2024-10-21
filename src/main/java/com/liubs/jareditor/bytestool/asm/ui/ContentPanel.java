package com.liubs.jareditor.bytestool.asm.ui;

import com.intellij.openapi.project.Project;
import com.liubs.jareditor.bytestool.asm.tree.BaseTreeNode;
import com.liubs.jareditor.bytestool.asm.tree.MethodTreeNode;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 右边的内容模块，动态展示字段/函数/接口等模块
 * @author Liubsyy
 * @date 2024/10/21
 */
public class ContentPanel extends JPanel {

    private Map<Class<?>,JPanel> panels = new HashMap<>();

    public ContentPanel(Project project) {
        setLayout(new BorderLayout());

        MethodPanel methodPanel = new MethodPanel(project);
        panels.put(MethodTreeNode.class, methodPanel);
    }

    public void refresh(BaseTreeNode selectedNode){
        this.removeAll();
        JPanel panel = panels.get(selectedNode.getClass());
        if(null == panel) {
            return;
        }
        if(panel instanceof IPanelRefresh) {
            IPanelRefresh panelRefresh = (IPanelRefresh)panel;
            panelRefresh.refresh(selectedNode);
            this.add(panel);
            this.revalidate();
            this.repaint();
        }
    }







}
