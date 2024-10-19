package com.liubs.jareditor.bytestool.asm.tree;

import com.intellij.icons.AllIcons;

import javax.swing.*;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class InterfaceTreeNode extends BaseTreeNode{
    public InterfaceTreeNode(String name) {
        super(name);
    }


    @Override
    public Icon icon() {
        return AllIcons.Nodes.Interface;
    }
}
