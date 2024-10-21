package com.liubs.jareditor.bytestool.asm.service;

import com.intellij.openapi.project.Project;

import com.liubs.jareditor.bytestool.asm.tree.InterfaceTreeNode;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class ASMClassService {
    private ClassNode classNode;

    public ASMClassService(byte[] bytes) {

        try ( ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            ClassReader reader = new ClassReader(inputStream);
            classNode = new ClassNode();
            reader.accept(classNode, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ClassNode getClassNode() {
        return classNode;
    }

}
