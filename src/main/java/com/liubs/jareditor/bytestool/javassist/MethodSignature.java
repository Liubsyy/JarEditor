package com.liubs.jareditor.bytestool.javassist;

import com.intellij.psi.*;
import com.liubs.jareditor.util.PsiFileUtil;
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

            String paramTypeText = PsiFileUtil.resoleGenericText(psiParameter.getType());

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
        if( ! (psiMember instanceof PsiMethod) ) {
            return psiMember.getText();
        }

        PsiMethod psiMethod = (PsiMethod)psiMember;
        psiMethod = (PsiMethod)psiMethod.copy();


        PsiParameter[] parameters = psiMethod.getParameterList().getParameters();
        Map<String, String> parameterReplacementMap = PsiFileUtil.resoleGenericParam(parameters);

        // 替换方法体内的引用
        psiMethod.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitReferenceExpression(PsiReferenceExpression expression) {
                super.visitReferenceExpression(expression);
                String refName = expression.getReferenceName();
                if (refName != null && parameterReplacementMap.containsKey(refName)) {
                    expression.handleElementRename(parameterReplacementMap.get(refName));
                }
            }

            @Override
            public void visitTypeElement(PsiTypeElement typeElement) {
                super.visitTypeElement(typeElement);
                PsiType originalType = typeElement.getType();
                PsiType newType = PsiFileUtil.replaceGenericType(originalType, typeElement);
                if (!newType.equals(originalType)) {
                    typeElement.replace(PsiFileUtil.createTypeElementFromType(newType, typeElement));
                }
            }
        });

        PsiCodeBlock psiCodeBlock = psiMethod.getBody();
        if(null == psiCodeBlock) {
            return psiMethod.getText();
        }
        return this.show() + psiCodeBlock.getText();

        /*int i = text.indexOf("{");
        int j = text.lastIndexOf("}");
        if(i<0 || j<0) {
            return text;
        }

        //用javassist的函数签名吧，省的替换泛型
        return show()+"{"+text.substring(i+1,j)+"}";
         */
    }





}
