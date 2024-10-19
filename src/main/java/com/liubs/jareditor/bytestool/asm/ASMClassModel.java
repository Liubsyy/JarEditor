package com.liubs.jareditor.bytestool.asm;

import com.intellij.openapi.project.Project;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayInputStream;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class ASMClassModel {
    private Project project;
    private ClassNode classNode;

    public ASMClassModel(Project project, byte[] bytes) {
        this.project = project;

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
