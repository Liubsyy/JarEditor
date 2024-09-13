package com.liubs.jareditor.bytestool.javassist;

import com.intellij.psi.PsiClassInitializer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMember;
import com.liubs.jareditor.util.StringUtils;
import javassist.CtConstructor;
import javassist.CtMember;

/**
 * 静态代码块
 * @author Liubsyy
 * @date 2024/9/13
 */
public class ClassInitializerSignature implements ISignature {

    private CtConstructor classInitializer;
    public ClassInitializerSignature(CtConstructor classInitializer){
       this.classInitializer = classInitializer;
    }

    @Override
    public String show() {
        return "static {...}";
    }

    @Override
    public boolean isSameTarget(PsiMember psiMember) {
        if( !(psiMember instanceof PsiClassInitializer) ) {
            return false;
        }

        return true;
    }

    @Override
    public CtMember getMember() {
        return classInitializer;
    }

    @Override
    public String convertToJavassistCode(PsiFile psiFile, PsiElement psiMember) {
        String text = psiMember.getText();
        if(StringUtils.isEmpty(text)) {
            return "static{\n\t//Insert static code here...\n}";
        }
        return text;
    }
}
