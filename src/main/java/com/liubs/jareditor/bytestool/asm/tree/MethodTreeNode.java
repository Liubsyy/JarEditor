package com.liubs.jareditor.bytestool.asm.tree;

import com.intellij.icons.AllIcons;
import org.objectweb.asm.tree.MethodNode;

import javax.swing.*;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class MethodTreeNode extends BaseTreeNode{
    private MethodNode methodNode;
    public MethodTreeNode(MethodNode methodNode) {
        super(methodNode.name);
        this.methodNode = methodNode;
    }

    @Override
    public Icon icon() {
        return AllIcons.Nodes.Method;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }
}
