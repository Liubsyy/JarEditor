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
     * 将参数进行范型擦除
     * 1. 形如T a，改成java.lang.Object
     * 2. 形如T [] a，改成java.lang.Object[]
     * 3.形如 java.lang.Class<T>，擦除<T>变成java.lang.Class
     * @param type
     * @return
     */
    public static String resoleGenericText(PsiType type) {
        if (type instanceof PsiPrimitiveType) {
            return type.getCanonicalText();
        } else if (type instanceof PsiArrayType) {
            PsiType componentType = ((PsiArrayType) type).getComponentType();
            return resoleGenericText(componentType) + "[]";
        } else if (type instanceof PsiClassType) {
            PsiClassType.ClassResolveResult resolveResult = ((PsiClassType) type).resolveGenerics();
            PsiClass psiClass = resolveResult.getElement();

            if (psiClass != null) {
                if (psiClass instanceof PsiTypeParameter) {
                    return CommonClassNames.JAVA_LANG_OBJECT;
                }
                String qualifiedName = psiClass.getQualifiedName();
                if (qualifiedName != null) {
                    PsiType[] parameters = ((PsiClassType) type).getParameters();
                    if (parameters.length > 0) {
                        // 泛型擦除：移除所有泛型参数
                        return qualifiedName;
                    } else {
                        return type.getCanonicalText();
                    }
                }
            }
        }
        return type.getCanonicalText();
    }

    public static Map<String, String> resoleGenericParam(PsiParameter[] parameters){
        Map<String, String> parameterReplacementMap = new HashMap<>();
        int index = 1;

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

        return parameterReplacementMap;
    }


    private static PsiTypeElement createTypeElementFromText(String typeText, PsiElement context) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(context.getProject());
        return factory.createTypeElementFromText(typeText, context);
    }

    public static PsiType replaceGenericType(PsiType type, PsiElement context) {
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

    public static PsiTypeElement createTypeElementFromType(PsiType type, PsiElement context) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(context.getProject());
        return factory.createTypeElement(type);
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
}
