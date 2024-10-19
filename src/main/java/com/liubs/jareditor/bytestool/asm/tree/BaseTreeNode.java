package com.liubs.jareditor.bytestool.asm.tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class BaseTreeNode extends DefaultMutableTreeNode {
    private Icon icon;
    public BaseTreeNode(String name) {
        this(name,null);
    }

    public BaseTreeNode(String name,Icon icon) {
        super(name);
        this.icon = icon;
    }

    public Icon icon(){
        return icon;
    }



}
