package com.liubs.jareditor.bytestool.asm.aggregate;

import org.objectweb.asm.tree.FieldNode;

/**
 * @author Liubsyy
 * @date 2024/10/23
 */
public class MyAssemblyField {
    private FieldNode fieldNode;

    public MyAssemblyField(FieldNode fieldNode) {
        this.fieldNode = fieldNode;
    }

    public String name(){
        return fieldNode.name;
    }

    public FieldNode getFieldNode() {
        return fieldNode;
    }
}
