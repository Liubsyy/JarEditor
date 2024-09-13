package com.liubs.jareditor.bytestool.javassist;


import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMember;
import javassist.CtMember;

/**
 * @author Liubsyy
 * @date 2024/8/28
 */

public interface ISignature {

    enum Type{
        FIELD("Field"),
        METHOD("Method"),
        CONSTRUCTOR("Constructor"),
        CLASS_INITIALIZER("ClassInitializer");
        public String name;

        Type(String name) {
            this.name = name;
        }
    }

    String show();


    boolean isSameTarget(PsiMember psiMember);

    default CtMember getMember(){
        return null;
    }

    default String convertToJavassistCode(PsiFile psiFile,PsiElement psiMember){
        return psiMember.getText();
    }

}
