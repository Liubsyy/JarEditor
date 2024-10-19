package com.liubs.jareditor.bytestool.asm.tree;

import com.intellij.icons.AllIcons;

import javax.swing.*;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class MethodTreeCategory extends BaseTreeNode{
    public MethodTreeCategory() {
        super("Method");
    }

    @Override
    public Icon icon() {
        return AllIcons.Nodes.Method;
    }
}
