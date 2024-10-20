package com.liubs.jareditor.bytestool.asm;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.util.Printer;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Liubsyy
 * @date 2024/10/20
 */
public class MyASMAnnotator implements Annotator {

    public static final TextAttributesKey LANGUAGE_KEYWORD = TextAttributesKey.createTextAttributesKey(
            "MY_LANGUAGE_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
    );

    private static Set<String> KEYWORDS = new HashSet<>();
    static {
        for(String e : Printer.OPCODES) {
            KEYWORDS.add(e.toLowerCase());
        }
    }

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof PsiIdentifier) {
            String text = element.getText();
            if (KEYWORDS.contains(text)) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                        .range(element)
                        .textAttributes(LANGUAGE_KEYWORD)
                        .create();
            }
        }
    }

}