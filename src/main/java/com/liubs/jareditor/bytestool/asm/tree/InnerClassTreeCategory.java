package com.liubs.jareditor.bytestool.asm.tree;

import com.intellij.icons.AllIcons;

import javax.swing.*;

/**
 * @author Liubsyy
 * @date 2024/10/23
 */
public class InnerClassTreeCategory extends BaseTreeNode{
    public InnerClassTreeCategory() {
        super("Inner Class");
    }

    @Override
    public Icon icon() {
        return AllIcons.FileTypes.JavaClass;
    }
}