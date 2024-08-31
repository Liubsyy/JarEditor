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
        int index = 1;
        Map<String, String> parameterReplacementMap = new HashMap<>();


        // 记录每个参数的替换规则
        for (PsiParameter parameter : parameters) {
            PsiType type = parameter.getType();
            String replacementType;

            // 处理泛型类型
            if (type instanceof PsiClassType) {
                PsiClassType classType = (PsiClassType) type;
                PsiType[] parametersOfClassType = classType.getParameters();
                if (parametersOfClassType.length > 0) {
                    // 泛型擦除
                    replacementType = classType.rawType().getCanonicalText();
                } else {
                    replacementType = type.getCanonicalText();
                }
            } else if (type instanceof PsiArrayType) {
                // 处理数组类型
                PsiType componentType = ((PsiArrayType) type).getComponentType();
                if (componentType instanceof PsiClassType) {
                    replacementType = "java.lang.Object[]";
                } else {
                    replacementType = type.getCanonicalText();
                }
            } else if (type instanceof PsiTypeParameter) {
                // 泛型类型参数，替换为java.lang.Object
                replacementType = "java.lang.Object";
            } else {
                replacementType = type.getCanonicalText();
            }

            String replacementName = "$" + index++;
            parameterReplacementMap.put(parameter.getName(), replacementName);
            // 设置新的类型和名字
            parameter.getTypeElement().replace(createTypeElementFromText(replacementType, parameter));
            parameter.setName(replacementName);
        }

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
                PsiType newType = replaceGenericType(originalType, typeElement);
                if (!newType.equals(originalType)) {
                    typeElement.replace(createTypeElementFromType(newType, typeElement));
                }
            }
        });

        String text = psiMethod.getText();
        return text;

        /*int i = text.indexOf("{");
        int j = text.lastIndexOf("}");
        if(i<0 || j<0) {
            return text;
        }

        //用javassist的函数签名吧，省的替换泛型
        return show()+"{"+text.substring(i+1,j)+"}";
         */
    }

    private PsiType replaceGenericType(PsiType type, PsiElement context) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(context.getProject());

        if (type instanceof PsiClassType) {
            PsiClassType classType = (PsiClassType) type;
            PsiType[] parameters = classType.getParameters();
            if (parameters.length > 0) {
                return factory.createTypeByFQClassName(classType.rawType().getCanonicalText());
            }
        } else if (type instanceof PsiArrayType) {
            PsiType componentType = ((PsiArrayType) type).getComponentType();
            PsiType newComponentType = replaceGenericType(componentType, context);
            return newComponentType.createArrayType();
        } else if (type instanceof PsiTypeParameter) {
            return PsiType.getJavaLangObject(PsiManager.getInstance(context.getProject()), context.getResolveScope());
        }
        return type;
    }

    private PsiTypeElement createTypeElementFromType(PsiType type, PsiElement context) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(context.getProject());
        return factory.createTypeElement(type);
    }


    private PsiTypeElement createTypeElementFromText(String typeText, PsiElement context) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(context.getProject());
        return factory.createTypeElementFromText(typeText, context);
    }


}
