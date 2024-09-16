package com.liubs.jareditor.bytestool.javassist;

import com.intellij.openapi.project.Project;
import com.intellij.util.PathUtil;
import com.liubs.jareditor.sdk.ProjectDependency;
import com.liubs.jareditor.util.StringUtils;
import javassist.*;

import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * @author Liubsyy
 * @date 2024/8/27
 */
public class JavassistClassHolder {

    private ClassPool classPool;
    private CtClass ctClass;


    //构造函数
    private List<CtConstructor> constructors = new ArrayList<>() ;
    //字段
    private List<CtField> fields = new ArrayList<>() ;
    //函数
    private List<CtMethod> methods = new ArrayList<>() ;
    //静态代码块
    private CtConstructor classInitializer;


    /**
     * 内部类，多个层级形成树形结构
     */
    private List<JavassistClassHolder> innerClasses = new ArrayList<>();

    //所有字段/函数/构造函数/静态代码块 成员
    private List<TargetUnit> memberUnits = new ArrayList<>();

    static {
        //javassist禁用jar连接缓存
        //如果开启可能会导致修改完的jar再次修改时读取异常
        ClassPool.cacheOpenedJarFile = false;
    }


    public JavassistClassHolder(Project project, byte[] bytes, String ... extraClassPath){
        classPool = new ClassPool();
        classPool.appendSystemPath();
        for(String path : extraClassPath) {
            try {
                classPool.appendClassPath(path);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        ProjectDependency.getDependentLib(project).forEach(c->{
            try {
                classPool.appendClassPath(PathUtil.getLocalPath(c.getPath()));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        });

        try ( ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            this.ctClass = classPool.makeClass(inputStream);
            refreshCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JavassistClassHolder(ClassPool classPool, CtClass ctClass) {
        this.classPool = classPool;
        this.ctClass = ctClass;
        try {
            refreshCache();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 递归初始化内部类(可能存在多层内部类)
     */
    public void initInnerClasses(){
        try {
            CtClass[] declaredClasses = ctClass.getDeclaredClasses();
            if(null == declaredClasses || declaredClasses.length ==0) {
                return;
            }
            for(CtClass ctInnerClass : declaredClasses) {
                if(ctInnerClass == ctClass) {
                    continue;
                }
                JavassistClassHolder innerClassJavassistClassHolder = new JavassistClassHolder(classPool, ctInnerClass);
                innerClasses.add(innerClassJavassistClassHolder);
                innerClassJavassistClassHolder.initInnerClasses();
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 深层遍历获取所有的内部类
     * 采用先序遍历，好处是父类和子类并排展示，符合正常人的浏览习惯
     * @return
     */
    public Map<String, JavassistClassHolder> getDepthClassHolders(){
        Map<String,JavassistClassHolder> allHolders = new LinkedHashMap<>();
        visitClassHolders(allHolders);
        return allHolders;
    }
    private void visitClassHolders(Map<String,JavassistClassHolder> allHolders){
        allHolders.put(ctClass.getName(),this);
        for(JavassistClassHolder innerClass : innerClasses) {
            innerClass.visitClassHolders(allHolders);
        }
    }

    public boolean refreshCache(){
        constructors.clear();
        fields.clear();
        methods.clear();

        constructors.addAll(Arrays.asList(ctClass.getConstructors()));
        fields.addAll(Arrays.asList(ctClass.getDeclaredFields()));
        methods.addAll(Arrays.asList(ctClass.getDeclaredMethods()));
        classInitializer = ctClass.getClassInitializer();
        return true;
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


    public CtConstructor getClassInitializer() {
        return classInitializer;
    }
    public CtConstructor createClassInitializer(){
        try {
            ctClass.makeClassInitializer();
            classInitializer = ctClass.getClassInitializer();
            return classInitializer;
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void imports(List<String> imports) {
        classPool.clearImportedPackages();
        for(String importStr : imports){
            importStr = importStr.replace("import ", "").replace(";","").trim();
            if(StringUtils.isEmpty(importStr)) {
                continue;
            }
            classPool.importPackage(importStr);
        }

    }

    public Result setBody(CtBehavior ctBehavior,String body){
        Result result = new Result(true,null);
        try {
            ctBehavior.setBody(body);
            result.setClassName(ctClass.getName());
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }
    public Result modifyField(CtField ctField,String newFieldSrc){
        Result result = new Result(true,null);
        try {
            CtField newField = CtField.make(newFieldSrc, ctClass);
            ctClass.removeField(ctField);
            ctClass.addField(newField);

            result.setClassName(ctClass.getName());
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
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

            result.setClassName(ctClass.getName());
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }
    public Result addMethod(String newMethodSrc){
        Result result = new Result(true,null);
        try {
            CtMethod method = CtMethod.make(newMethodSrc, ctClass);
            ctClass.addMethod(method);

            result.setClassName(ctClass.getName());
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }
    public Result addConstructor(String newConstructorSrc){
        Result result = new Result(true,null);
        try {
            CtConstructor constructor = CtNewConstructor.make(newConstructorSrc,ctClass);
            ctClass.addConstructor(constructor);

            result.setClassName(ctClass.getName());
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
            ctClass.removeField(ctField);

            result.setClassName(ctClass.getName());
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }

    public Result deleteMethod(CtMethod ctMethod){
        Result result = new Result(true,null);
        try {
            ctClass.removeMethod(ctMethod);

            result.setClassName(ctClass.getName());
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }
    public Result deleteConstructor(CtConstructor constructor){
        Result result = new Result(true,null);
        try {
            ctClass.removeConstructor(constructor);

            result.setClassName(ctClass.getName());
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }
    public Result deleteClassInitializer(){
        Result result = new Result(true,null);
        try {
            CtConstructor classInitializer = ctClass.getClassInitializer();
            if(null != classInitializer) {
                ctClass.removeConstructor(classInitializer);
            }
            this.classInitializer = null;

            result.setClassName(ctClass.getName());
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }



    public Result insertBefore(CtBehavior ctBehavior,String body){
        Result result = new Result(true,null);
        try {
            ctBehavior.insertBefore(body);

            result.setClassName(ctClass.getName());
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }
    public Result insertAfter(CtBehavior ctBehavior,String body){
        Result result = new Result(true,null);
        try {
            ctBehavior.insertAfter(body);
            result.setClassName(ctClass.getName());
            result.setBytes(ctClass.toBytecode());
            ctClass.defrost();
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
        return result;
    }

    public List<TargetUnit> initMemberTarget(){
        memberUnits.clear();
        for(CtConstructor constructor : getConstructors()){
            try {
                memberUnits.add(new TargetUnit(ISignature.Type.CONSTRUCTOR, new ConstructorSignature(constructor)));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }

        for(CtField ctField : getFields()){
            try {
                memberUnits.add(new TargetUnit(ISignature.Type.FIELD, new FieldSignature(ctField)));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }

        CtConstructor classInitializer = getClassInitializer();
        memberUnits.add(new TargetUnit(ISignature.Type.CLASS_INITIALIZER,new ClassInitializerSignature(classInitializer)));

        for(CtMethod ctMethod : getMethods()){
            try {
                memberUnits.add(new TargetUnit(ISignature.Type.METHOD, new MethodSignature(ctMethod)));
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
        return memberUnits;
    }

    public List<TargetUnit> getMemberUnits() {
        return memberUnits;
    }

    public String getClassName(){
        return ctClass.getName();
    }

    public static class Result {
        private final boolean success;
        private final String err;
        private String className;
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

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }
    }



}
