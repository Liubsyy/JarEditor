package com.liubs.jareditor.bytestool.javassist;

import com.intellij.psi.*;
import javassist.*;


/**
 * @author Liubsyy
 * @date 2024/8/28
 */
public class FieldSignature implements ISignature{

    private int modifier;
    private String fieldType;
    private String fieldName;
    private CtField ctField;

    public FieldSignature(CtField field) throws NotFoundException {
        // 获取修饰符
        this.modifier = field.getModifiers();

        // 获取字段类型
        this.fieldType = field.getType().getName();

        //获取字段名称
        this.fieldName = field.getName();

        this.ctField = field;
    }

    @Override
    public CtMember getMember() {
        return ctField;
    }

    @Override
    public String show() {

        String modifiers = Modifier.toString(modifier);

        return String.format("%s %s %s", modifiers, fieldType,fieldName);
    }
    @Override
    public boolean isSameTarget(PsiMember psiMember) {
        if( !(psiMember instanceof PsiField) ){
            return false;
        }
        PsiField psiField = (PsiField)psiMember;
        if( !fieldName.equals(psiField.getName()) ){
            return false;
        }
        return true;
    }

    @Override
    public String convertToJavassistCode(PsiFile psiFile, PsiElement psiMember) {

        if( ! (psiMember instanceof PsiField) ) {
            return psiMember.getText();
        }
        PsiField psiField = (PsiField)psiMember;

        /*
        // 获取字段的修饰符列表（包括注解）
        PsiModifierList modifierList = psiField.getModifierList();

        // 构建字段声明部分
        StringBuilder fieldDeclarationBuilder = new StringBuilder();

        // 获取修饰符（例如private, static等），并排除注解
        if (modifierList != null) {
            for (PsiElement element : modifierList.getChildren()) {
                if (!(element instanceof PsiAnnotation)) {
                    fieldDeclarationBuilder.append(element.getText()).append(" ");
                }
            }
        }

        // 添加字段的类型和名称
        fieldDeclarationBuilder.append(psiField.getType().getPresentableText()).append(" ");
        fieldDeclarationBuilder.append(psiField.getName());
*/

        StringBuilder fieldDeclarationBuilder = new StringBuilder(this.show());


        // 如果字段有初始化器，添加初始化部分
        if (psiField.getInitializer() != null) {
            fieldDeclarationBuilder.append(" = ").append(psiField.getInitializer().getText());
        }

        // 结束字段声明
        fieldDeclarationBuilder.append(";");

        // 返回去除注解后的字段声明字符串
        return fieldDeclarationBuilder.toString();
    }
}
