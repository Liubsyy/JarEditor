package com.liubs.jareditor.bytestool.asm.entity;

/**
 * @author Liubsyy
 * @date 2024/10/21
 */
public class MyLineNumber {
    private int lineEditor;
    private int labelIndex;
    private int lineSource;

    public int getLineEditor() {
        return lineEditor;
    }

    public void setLineEditor(int lineEditor) {
        this.lineEditor = lineEditor;
    }

    public int getLabelIndex() {
        return labelIndex;
    }

    public void setLabelIndex(int labelIndex) {
        this.labelIndex = labelIndex;
    }

    public int getLineSource() {
        return lineSource;
    }

    public void setLineSource(int lineSource) {
        this.lineSource = lineSource;
    }
}
