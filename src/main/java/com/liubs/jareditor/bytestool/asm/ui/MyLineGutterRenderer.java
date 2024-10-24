package com.liubs.jareditor.bytestool.asm.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.liubs.jareditor.bytestool.asm.entity.MyLineNumber;
import com.liubs.jareditor.bytestool.asm.ui.common.TextIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Liubsyy
 * @date 2024/10/20
 */
public class MyLineGutterRenderer extends GutterIconRenderer {
    // 定义一个唯一的 Key 来标记您的 Highlighter
    private static final Key<Boolean> ASM_LABEL_LINE = Key.create("liubsyy_ASM_LABAL_LINE");

    private int labelIndex; //第几个Label
    private int editorLine; //编辑器所在行数
    private int sourceCodeLine; //源码行 linenumber

    private TextIcon textIcon;
    private String tooltipText;

    public MyLineGutterRenderer(int labelIndex, int editorLine, int sourceCodeLine) {
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
        MyLineGutterRenderer that = (MyLineGutterRenderer) o;
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


    public static void markLineLighter(Editor editor, List<MyLineNumber> lineNumberList){

        MarkupModel markupModel = editor.getMarkupModel();
        Document document = editor.getDocument();

        // 收集需要移除的 Highlighter
        List<RangeHighlighter> toRemove = new ArrayList<>();
        for (RangeHighlighter highlighter : markupModel.getAllHighlighters()) {
            if (Boolean.TRUE.equals(highlighter.getUserData(ASM_LABEL_LINE))) {
                toRemove.add(highlighter);
            }
        }
        // 移除旧的 Highlighter
        for (RangeHighlighter highlighter : toRemove) {
            markupModel.removeHighlighter(highlighter);
        }

        for(MyLineNumber eachLine : lineNumberList) {
            int lineNumber = eachLine.getLineEditor();
            int lineStartOffset = document.getLineStartOffset(lineNumber);
            int lineEndOffset = document.getLineEndOffset(lineNumber);

            // 创建 RangeHighlighter
            RangeHighlighter highlighter = markupModel.addRangeHighlighter(
                    lineStartOffset,
                    lineEndOffset,
                    HighlighterLayer.FIRST,
                    null,
                    HighlighterTargetArea.LINES_IN_RANGE
            );

            // 设置自定义的 GutterIconRenderer
            highlighter.setGutterIconRenderer(new MyLineGutterRenderer(eachLine.getLabelIndex(),lineNumber,eachLine.getLineSource()));
            highlighter.putUserData(ASM_LABEL_LINE, Boolean.TRUE);
        }
    }

}
