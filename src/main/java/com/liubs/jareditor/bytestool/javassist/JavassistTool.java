package com.liubs.jareditor.bytestool.javassist;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.sdk.ProjectDependency;
import javassist.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/8/27
 */
public class JavassistTool {

    private ClassPool classPool = new ClassPool();
    private List<CtField> fields = new ArrayList<>();
    private List<CtMethod> methods = new ArrayList<>();

    public JavassistTool(Project project,VirtualFile virtualFile){
        ProjectDependency.getDependentLib(project).forEach(c->{
            try {
                classPool.appendClassPath(c.getPath());
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        });

        try (InputStream inputStream = virtualFile.getInputStream()) {
            CtClass ctClass = classPool.makeClass(inputStream);

            // 获取类的所有字段
            for (CtField field : ctClass.getDeclaredFields()) {
                fields.add(field);
            }

            for (CtMethod method : ctClass.getDeclaredMethods()) {
                methods.add(method);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CtMethod> getMethods() {
        return methods;
    }


    public static String getMethodSignature(CtMethod method) throws NotFoundException {
        // 获取修饰符
        int mod = method.getModifiers();
        String modifiers = Modifier.toString(mod);

        // 获取返回类型
        String returnType = method.getReturnType().getName();

        // 获取方法名称
        String methodName = method.getName();

        // 获取参数类型
        CtClass[] parameterTypes = method.getParameterTypes();
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                params.append(", ");
            }
            params.append(parameterTypes[i].getName())
                    .append(" $")
                    .append(i + 1);
        }

        // 构造方法签名
        return String.format("%s %s %s(%s);", modifiers, returnType, methodName, params.toString());
    }
}
