package com.liubs.jareditor.bytestool.javassist;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMember;
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
}
