package com.liubs.jareditor.bytestool.javassist;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMember;

import java.util.Objects;

/**
 * @author Liubsyy
 * @date 2024/8/28
 */
public class TargetUnit {

    private ISignature.Type type;
    private ISignature targetSignature;

    public TargetUnit(ISignature.Type type, ISignature targetSignature) {
        this.type = type;
        this.targetSignature = targetSignature;
    }

    @Override
    public String toString() {
        return null == targetSignature ? type.name : targetSignature.show()+";";
    }

    public ISignature.Type getType() {
        return type;
    }

    public ISignature getTargetSignature() {
        return targetSignature;
    }

    public boolean isSameTarget(PsiMember psiMember) {
        if(null == psiMember || null == targetSignature){
            return false;
        }
        return targetSignature.isSameTarget(psiMember);
    }

    public String convertToJavassistCode(PsiFile psiFile,PsiElement psiMember){
        if(targetSignature == null) {
            return psiMember.getText();
        }
        return targetSignature.convertToJavassistCode(psiFile,psiMember);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetUnit that = (TargetUnit) o;
        if(targetSignature == null && that.targetSignature == null) {
            return true;
        }
        if(type == that.type) {
            if(null != targetSignature && null != that.targetSignature) {
                return targetSignature.getMember() == that.targetSignature.getMember();
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        if(null == targetSignature) {
            return Objects.hash(type);
        }
        if(null != targetSignature.getMember()) {
            return targetSignature.getMember().hashCode();
        }
        return Objects.hash(type, targetSignature);
    }
}
