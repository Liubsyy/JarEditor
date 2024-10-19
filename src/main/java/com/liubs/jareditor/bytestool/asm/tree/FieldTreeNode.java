package com.liubs.jareditor.bytestool.asm.tree;

import com.intellij.icons.AllIcons;
import org.objectweb.asm.tree.FieldNode;

import javax.swing.*;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class FieldTreeNode extends BaseTreeNode{
    private FieldNode fieldNode;
    public FieldTreeNode(FieldNode fieldNode) {
        super(fieldNode.name);
        this.fieldNode = fieldNode;
    }

    @Override
    public Icon icon() {
        return AllIcons.Nodes.Field;
    }
}
