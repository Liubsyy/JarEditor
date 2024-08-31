package com.liubs.jareditor.bytestool.javassist;

import com.intellij.openapi.project.Project;
import com.intellij.util.PathUtil;
import com.liubs.jareditor.sdk.ProjectDependency;
import com.liubs.jareditor.util.StringUtils;
import javassist.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/8/27
 */
public class JavassistTool {

    private ClassPool classPool;

    private CtClass ctClass;
    private List<CtConstructor> constructors ;
    private List<CtField> fields ;
    private List<CtMethod> methods;

    public JavassistTool(Project project, InputStream classInputStream){
        classPool = new ClassPool();
        constructors = new ArrayList<>();
        fields = new ArrayList<>();
        methods = new ArrayList<>();

        classPool.appendSystemPath();
        ProjectDependency.getDependentLib(project).forEach(c->{
            try {
                classPool.appendClassPath(PathUtil.getLocalPath(c.getPath()));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        });

        try (InputStream inputStream = classInputStream) {
            this.ctClass = classPool.makeClass(inputStream);

            constructors.addAll(Arrays.asList(ctClass.getConstructors()));
            fields.addAll(Arrays.asList(ctClass.getDeclaredFields()));
            methods.addAll(Arrays.asList(ctClass.getDeclaredMethods()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<CtConstructor> getConstructors() {
        return constructors;
    }

    public List<CtMethod> getMethods() {
        return methods;
    }

    public List<CtField> getFields() {
        return fields;
    }



    public void imports(String[] imports) {
        classPool.clearImportedPackages();
        for(String importStr : imports){
            importStr = importStr.replace("import ", "").replace(";","").trim();
            if(StringUtils.isEmpty(importStr)) {
                continue;
            }
            classPool.importPackage(importStr);
        }

    }

    public Result setBody(CtMethod ctMethod,String body){
        Result result = new Result(true,null);
        try {
            ctMethod.setBody(body);
            result.setBytes(ctMethod.getDeclaringClass().toBytecode());
            ctMethod.getDeclaringClass().defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }
    public Result modifyField(CtField ctField,String newFieldSrc){
        Result result = new Result(true,null);
        try {
            CtField newField = CtField.make(newFieldSrc, ctField.getDeclaringClass());
            ctField.getDeclaringClass().removeField(ctField);
            ctField.getDeclaringClass().addField(newField);

            result.setBytes(ctField.getDeclaringClass().toBytecode());
            ctField.getDeclaringClass().defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }
    public Result addField(String newFieldSrc){
        Result result = new Result(true,null);
        try {
            CtField newField = CtField.make(newFieldSrc, ctClass);
            ctClass.addField(newField);

            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }

    public Result deleteField(CtField ctField){
        Result result = new Result(true,null);
        try {
            ctField.getDeclaringClass().removeField(ctField);
            result.setBytes(ctField.getDeclaringClass().toBytecode());
            ctField.getDeclaringClass().defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }



    public Result insertBefore(CtMethod ctMethod,String body){
        Result result = new Result(true,null);
        try {
            ctMethod.insertBefore(body);
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }
    public Result insertAfter(CtMethod ctMethod,String body){
        Result result = new Result(true,null);
        try {
            ctMethod.insertAfter(body);
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }


    public static class Result {
        private final boolean success;
        private final String err;
        private byte[] bytes;

        public Result(boolean success, String err) {
            this.success = success;
            this.err = err;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErr() {
            return err;
        }

    }

}
