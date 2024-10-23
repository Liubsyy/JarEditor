package com.liubs.jareditor.bytestool.asm.ui;

import com.intellij.ui.treeStructure.SimpleTree;
import com.liubs.jareditor.bytestool.asm.aggregate.MyAssemblyClass;
import com.liubs.jareditor.bytestool.asm.aggregate.MyAssemblyField;
import com.liubs.jareditor.bytestool.asm.aggregate.MyAssemblyMethod;
import com.liubs.jareditor.bytestool.asm.tree.*;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/10/21
 */
public class MyTree extends SimpleTree {


    public BaseTreeNode initNodes(MyAssemblyClass asmClassService) {
        BaseTreeNode rootNode = new BaseTreeNode("Root");

        ClassInfoTreeNode classInfoTreeNode = new ClassInfoTreeNode(asmClassService);

        InterfaceTreeCategory interfacesNode = new InterfaceTreeCategory();
        List<String> interfaces = asmClassService.getClassNode().interfaces;
        for(String e : interfaces) {
            interfacesNode.add(new InterfaceTreeNode(e));
        }

        InnerClassTreeCategory innerClassTreeCategory = new InnerClassTreeCategory();
        List<InnerClassNode> innerClasses = asmClassService.getClassNode().innerClasses;
        for(InnerClassNode e : innerClasses) {
            innerClassTreeCategory.add(new InnerClassTreeNode(e));
        }

        FieldTreeCategory fieldsNode = new FieldTreeCategory();
        List<FieldNode> fields = asmClassService.getClassNode().fields;
        for(FieldNode fieldNode : fields) {
            fieldsNode.add(new FieldTreeNode(new MyAssemblyField(fieldNode)));
        }

        MethodTreeCategory methodsNode = new MethodTreeCategory();
        List<MethodNode> methods = asmClassService.getClassNode().methods;
        for(MethodNode method : methods) {
            methodsNode.add(new MethodTreeNode(new MyAssemblyMethod(method)));
        }

        rootNode.add(classInfoTreeNode);
        rootNode.add(interfacesNode);
        rootNode.add(innerClassTreeCategory);
        rootNode.add(fieldsNode);
        rootNode.add(methodsNode);

        this.setModel(new DefaultTreeModel(rootNode));
        this.setRootVisible(false);

        // 设置自定义渲染器
        this.setCellRenderer(new MyTreeCellRenderer());

        //展开方法节点
        this.expandNode(methodsNode);

        return rootNode;
    }


    // 递归展开指定的节点
    private void expandNode(BaseTreeNode node) {
        // 获取节点路径
        TreePath path = new TreePath(node.getPath());

        // 展开该路径
        this.expandPath(path);

        // 如果需要，可以递归展开子节点
        for (int i = 0; i < node.getChildCount(); i++) {
            BaseTreeNode childNode = (BaseTreeNode) node.getChildAt(i);
            expandNode(childNode);  // 递归展开子节点
        }
    }

    // 选中指定的节点
    public void selectNode(BaseTreeNode node) {
        // 获取节点的路径
        TreePath path = new TreePath(node.getPath());

        // 设置选中路径
        this.setSelectionPath(path);

        // 确保选中的节点可见
        this.scrollPathToVisible(path);
    }
}
