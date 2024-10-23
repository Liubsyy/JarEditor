package com.liubs.jareditor.bytestool.asm.aggregate;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayInputStream;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class MyAssemblyClass {
    private ClassNode classNode;

    public MyAssemblyClass(byte[] bytes) {

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
