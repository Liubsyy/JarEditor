package com.liubs.jareditor.util;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Liubsyy
 * @date 2024/8/30
 */
public class PsiFileUtil {


    /**
     * 将参数进行泛型擦除，返回擦除后的类型
     * 1. 形如T a，改成java.lang.Object
     * 2. 形如T [] a，改成java.lang.Object[]
     * 3.形如 java.lang.Class<T>，擦除<T>变成java.lang.Class
     * @param psiType
     * @return
     */
    public static String resoleGenericType(PsiType psiType) {
        if (psiType instanceof PsiPrimitiveType) {
            return psiType.getCanonicalText();
        } else if (psiType instanceof PsiArrayType) {
            PsiType componentType = ((PsiArrayType) psiType).getComponentType();
            return resoleGenericType(componentType) + "[]";
        } else if (psiType instanceof PsiClassType) {
            PsiClassType.ClassResolveResult resolveResult = ((PsiClassType) psiType).resolveGenerics();
            PsiClass psiClass = resolveResult.getElement();

            if (psiClass != null) {
                if (psiClass instanceof PsiTypeParameter) {
                    return CommonClassNames.JAVA_LANG_OBJECT;
                }
                String qualifiedName = psiClass.getQualifiedName();
                if (qualifiedName != null) {
                    PsiType[] parameters = ((PsiClassType) psiType).getParameters();
                    if (parameters.length > 0) {
                        // 泛型擦除：移除所有泛型参数
                        return qualifiedName;
                    } else {
                        return psiType.getCanonicalText();
                    }
                }
            }
        }
        return psiType.getCanonicalText();
    }

    /**
     * 将参数名替换成$1,$2,$3，并擦除泛型
     * @param parameters
     * @return paramName=>$i
     */
    public static Map<String, String> resolePsiParams(PsiParameter[] parameters){
        Map<String, String> parameterReplacementMap = new HashMap<>();
        for(int i = 0; i<parameters.length ;i++) {
            PsiParameter parameter = parameters[i];

            //参数替换成$1,$2,$3
            String replacementName = "$" + (i+1);
            parameterReplacementMap.put(parameter.getName(), replacementName);
            parameter.setName(replacementName);

            //泛型擦除
            replacePsiTypeElementIfGeneric(parameter);
        }
        return parameterReplacementMap;
    }


    /**
     * 如果是泛型参数的话，替换为新类型
     * @param psiParameter 参数
     */
    public static void replacePsiTypeElementIfGeneric(PsiParameter psiParameter){
        replacePsiTypeElementIfGeneric(psiParameter.getTypeElement());
    }
    public static void replacePsiTypeElementIfGeneric(PsiTypeElement psiTypeElement){
        if(null == psiTypeElement) {
            return;
        }
        try{
            PsiType psiType = psiTypeElement.getType();
            String resoleText = resoleGenericType(psiType);
            if(StringUtils.isNotEmpty(resoleText) && !resoleText.equals(psiType.getCanonicalText())) {
                psiTypeElement.replace(createTypeElementFromText(resoleText, psiTypeElement));
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }


    public static PsiTypeElement createTypeElementFromText(String typeText, PsiElement context) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(context.getProject());
        return factory.createTypeElementFromText(typeText, context);
    }



    /**
     * 获取Class中所有import语句
     * @param psiFile
     * @return
     */
    public static List<String> getAllImports(PsiFile psiFile) {
        List<String> imports = new ArrayList<>();
        if (!(psiFile instanceof PsiJavaFile)) {
            return imports;
        }

        PsiJavaFile javaFile = (PsiJavaFile) psiFile;
        PsiImportList importList = javaFile.getImportList();

        if (importList == null) {
            return imports; // 如果没有import语句，返回空
        }

        for (PsiImportStatementBase importStatement : importList.getAllImportStatements()) {
            String text = importStatement.getText();

            if(null != text) {
                imports.add(text.trim());
            }
        }

        return imports;
    }


    /**
     * 获取全类名，包括内部类用$隔开
     * @param psiClassPri
     * @return 结果可以直接反射获取Class
     */
    public static String getFullClassNameIncludingPackage(PsiClass psiClassPri) {
        PsiClass psiClass = psiClassPri;
        StringBuilder classNameBuilder = new StringBuilder();
        while (psiClass != null) {
            if (classNameBuilder.length() > 0) {
                classNameBuilder.insert(0, "$");
            }
            classNameBuilder.insert(0, psiClass.getName());
            PsiElement parent = psiClass.getParent();
            while (!(parent instanceof PsiClass) && parent != null) {
                parent = parent.getParent();
            }
            psiClass = (parent instanceof PsiClass) ? (PsiClass) parent : null;
        }

        PsiFile psiFile = psiClassPri.getContainingFile();
        if (psiFile instanceof PsiJavaFile) {
            String packageName = ((PsiJavaFile) psiFile).getPackageName();
            if (!packageName.isEmpty()) {
                classNameBuilder.insert(0, ".");
                classNameBuilder.insert(0, packageName);
            }
        }
        return classNameBuilder.toString();
    }
}
