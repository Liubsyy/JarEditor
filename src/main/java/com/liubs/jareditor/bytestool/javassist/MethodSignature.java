package com.liubs.jareditor.bytestool.javassist;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.liubs.jareditor.util.PsiFileUtil;
import com.liubs.jareditor.util.StringUtils;
import javassist.*;

import java.util.*;

/**
 * @author Liubsyy
 * @date 2024/8/28
 */
public class MethodSignature implements ISignature{

    private int modifier;
    private String returnType;
    private String methodName;
    private List<String> paramTypes;
    private CtMethod ctMethod;

    public MethodSignature(CtMethod method) throws NotFoundException {

        // 获取修饰符
        this.modifier = method.getModifiers();

        // 获取返回类型
        this.returnType = method.getReturnType().getName();

        // 获取方法名称
        this.methodName = method.getName();

        // 获取参数类型
        this.paramTypes = new ArrayList<>();
        for (CtClass parameterType : method.getParameterTypes()) {
            this.paramTypes.add(parameterType.getName());
        }

        this.ctMethod = method;
    }

    @Override
    public CtMember getMember() {
        return ctMethod;
    }

    @Override
    public String show() {

        String modifiers = Modifier.toString(modifier);

        StringBuilder params = new StringBuilder();
        for (int i = 0,len=paramTypes.size(); i < len; i++) {
            if (i > 0) {
                params.append(", ");
            }
            params.append(paramTypes.get(i))
                    .append(" $")
                    .append(i + 1);
        }

        return String.format("%s %s %s(%s)", modifiers, returnType, methodName, params.toString());
    }

    @Override
    public boolean isSameTarget(PsiMember psiMember) {
        if(!(psiMember instanceof PsiMethod)) {
            return false;
        }
        PsiMethod psiMethod = (PsiMethod)psiMember;
        if( !methodName.equals(psiMethod.getName()) ){
            return false;
        }
        PsiParameterList parameterList = psiMethod.getParameterList();
        if(parameterList.getParametersCount() != paramTypes.size()){
            return false;
        }
        for(int i = 0,len=paramTypes.size() ;i< len ;i++) {
            PsiParameter psiParameter = parameterList.getParameter(i);
            if(null == psiParameter) {
                return false;
            }

            String paramTypeText = PsiFileUtil.resoleGenericType(psiParameter.getType());

            if(!paramTypes.get(i).equals(paramTypeText)){
                return false;
            }
        }
        return true;
    }



    /**
     * 函数替换javassist风格
     * 1.参数和代码块引用的参数替换成 $1,$2这样的格式
     * 2.泛型擦除
     * @param psiFile
     * @param psiMember
     * @return
     */
    @Override
    public String convertToJavassistCode(PsiFile psiFile,PsiElement psiMember) {
        return convertToJavassistCode0(psiFile,psiMember);
    }

    public static String convertToJavassistCode0(PsiFile psiFile,PsiElement psiMember) {
        if( ! (psiMember instanceof PsiMethod) ) {
            return psiMember.getText();
        }

        PsiMethod psiMethod = (PsiMethod)psiMember;
        psiMethod = (PsiMethod)psiMethod.copy();

        //参数: 替换为$i并擦除泛型
        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        Map<String, String> parameterReplacementMap = PsiFileUtil.resolePsiParams(parameters);

        // 移除注解
        try{
            PsiModifierList modifierList = psiMethod.getModifierList();
            PsiAnnotation[] annotations = modifierList.getAnnotations();
            for (PsiAnnotation annotation : annotations) {
                annotation.delete();
            }
        }catch (Exception ex){}

        //擦除泛型
        psiMethod.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitTypeElement(PsiTypeElement typeElement) {
                super.visitTypeElement(typeElement);
                PsiFileUtil.replacePsiTypeElementIfGeneric(typeElement);
            }
        });

        //将方法体引用的参数替换成$1,$2,$3
        psiMethod.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitReferenceExpression(PsiReferenceExpression expression) {
                super.visitReferenceExpression(expression);

                //如果是this.field和super.field就不要替换了，肯定不是参数
                PsiExpression qualifier = expression.getQualifierExpression();
                if(null != qualifier){
                    String qualifierText = qualifier.getText();
                    if ("this".equals(qualifierText) || "super".equals(qualifierText)) {
                        return;
                    }
                }

                String refName = expression.getReferenceName();
                if (refName != null && parameterReplacementMap.containsKey(refName)) {
                    expression.handleElementRename(parameterReplacementMap.get(refName));
                }
            }
        });

        PsiCodeBlock methodBody = psiMethod.getBody();
        if (methodBody == null) {
            return psiMethod.getText();
        }


        return psiMethod.getText();

        //一种投机取巧的方式，取javassist的函数签名+PsiMethod的方法体
//        return this.show() + psiCodeBlock.getText();
    }





}
