package com.liubs.jareditor.bytestool.asm.instn;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

/**
 * @author Liubsyy
 * @date 2024/10/20
 */
public class MyGutterIconRenderer extends GutterIconRenderer {

    private int labelIndex; //第几个Label
    private int editorLine; //编辑器所在行数
    private int sourceCodeLine; //源码行 linenumber

    private TextIcon textIcon;
    private String tooltipText;

    public MyGutterIconRenderer(int labelIndex, int editorLine, int sourceCodeLine) {
        this.labelIndex = labelIndex;
        this.editorLine = editorLine;
        this.sourceCodeLine = sourceCodeLine;

        this.textIcon = new TextIcon("L"+labelIndex);
        this.tooltipText = "Linenumber:"+sourceCodeLine;
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return textIcon;
    }

    @Override
    public AnAction getClickAction() {
        return new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                // 定义点击图标时的行为
                Messages.showMessageDialog("图标被点击！", "信息", Messages.getInformationIcon());
            }
        };
    }

    @Override
    public Alignment getAlignment() {
        return Alignment.RIGHT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyGutterIconRenderer that = (MyGutterIconRenderer) o;
        return labelIndex == that.labelIndex && editorLine == that.editorLine && sourceCodeLine == that.sourceCodeLine;
    }

    @Override
    public int hashCode() {
        return Objects.hash(labelIndex, editorLine, sourceCodeLine);
    }

    @Override
    public String getTooltipText() {
        return tooltipText;
    }


}
