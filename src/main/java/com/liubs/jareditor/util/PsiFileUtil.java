package com.liubs.jareditor.util;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

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
