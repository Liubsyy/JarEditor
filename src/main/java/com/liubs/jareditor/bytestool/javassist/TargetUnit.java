package com.liubs.jareditor.bytestool.javassist;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMember;

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


}
