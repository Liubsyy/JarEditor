package com.liubs.jareditor.bytestool.javassist;

import com.intellij.psi.*;
import com.liubs.jareditor.util.PsiFileUtil;
import javassist.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Liubsyy
 * @date 2024/8/28
 */
public class ConstructorSignature implements ISignature{

    private int modifier;
    private String constructName;
    private String simpleMethodName;

    private List<String> paramTypes;
    private CtConstructor constructor;

    public ConstructorSignature(CtConstructor constructor) throws NotFoundException {

        // 获取修饰符
        this.modifier = constructor.getModifiers();

        // 获取构造函数名称
        this.constructName = this.simpleMethodName = constructor.getName();
        if(constructName.contains("$")){
            int lastIndexOf = constructName.lastIndexOf("$");
            if( lastIndexOf+1 < constructName.length() ) {
                simpleMethodName = simpleMethodName.substring(lastIndexOf+1);
            }
        }

        // 获取参数类型
        this.paramTypes = new ArrayList<>();
        for (CtClass parameterType : constructor.getParameterTypes()) {
            this.paramTypes.add(parameterType.getName());
        }

        this.constructor = constructor;

    }

    @Override
    public CtMember getMember() {
        return constructor;
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

        return String.format("%s %s(%s)", modifiers, constructName, params.toString());
    }

    @Override
    public boolean isSameTarget(PsiMember psiMember) {
        if(!(psiMember instanceof PsiMethod)) {
            return false;
        }
        PsiMethod psiMethod = (PsiMethod)psiMember;
        if( !constructName.equals(psiMethod.getName()) && !simpleMethodName.equals(psiMethod.getName()) ){
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

            PsiType type = psiParameter.getType();
            String paramTypeText = PsiFileUtil.resoleGenericType(type);

            if(!paramTypes.get(i).equals(paramTypeText)){
                return false;
            }
        }
        return true;
    }


    @Override
    public String convertToJavassistCode(PsiFile psiFile,PsiElement psiMember) {
        return MethodSignature.convertToJavassistCode0(psiFile,psiMember);
    }


}
