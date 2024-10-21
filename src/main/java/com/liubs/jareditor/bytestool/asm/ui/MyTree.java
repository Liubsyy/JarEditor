package com.liubs.jareditor.bytestool.asm.ui;

import com.intellij.ui.treeStructure.SimpleTree;
import com.liubs.jareditor.bytestool.asm.service.ASMClassService;
import com.liubs.jareditor.bytestool.asm.tree.*;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import javax.swing.tree.DefaultTreeModel;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/10/21
 */
public class MyTree extends SimpleTree {

    public void initNodes(ASMClassService asmClassService) {
        BaseTreeNode rootNode = new BaseTreeNode("Root");

        InterfaceTreeCategory interfacesNode = new InterfaceTreeCategory();
        List<String> interfaces = asmClassService.getClassNode().interfaces;
        for(String e : interfaces) {
            interfacesNode.add(new InterfaceTreeNode(e));
        }

        FieldTreeCategory fieldsNode = new FieldTreeCategory();
        List<FieldNode> fields = asmClassService.getClassNode().fields;
        for(FieldNode fieldNode : fields) {
            fieldsNode.add(new FieldTreeNode(fieldNode));
        }

        MethodTreeCategory methodsNode = new MethodTreeCategory();
        List<MethodNode> methods = asmClassService.getClassNode().methods;
        for(MethodNode method : methods) {
            methodsNode.add(new MethodTreeNode(method));
        }

        rootNode.add(interfacesNode);
        rootNode.add(fieldsNode);
        rootNode.add(methodsNode);

        this.setModel(new DefaultTreeModel(rootNode));
        this.setRootVisible(false);
        // 4. 设置自定义渲染器
        this.setCellRenderer(new MyTreeCellRenderer());
    }
}
