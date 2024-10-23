package com.liubs.jareditor.bytestool.asm.tree;

import com.intellij.icons.AllIcons;
import com.liubs.jareditor.bytestool.asm.aggregate.MyAssemblyClass;
import org.objectweb.asm.tree.ClassNode;

import javax.swing.*;

/**
 * @author Liubsyy
 * @date 2024/10/23
 */
public class ClassInfoTreeNode extends BaseTreeNode{
    private MyAssemblyClass myAssemblyClass;

    public ClassInfoTreeNode(MyAssemblyClass myAssemblyClass) {
        super("Class Info");
        this.myAssemblyClass = myAssemblyClass;
    }

    public ClassNode getClassNode() {
        return myAssemblyClass.getClassNode();
    }

    @Override
    public Icon icon() {
        return AllIcons.Nodes.Class;
    }

}
