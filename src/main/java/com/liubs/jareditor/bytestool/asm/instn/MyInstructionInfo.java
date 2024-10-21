package com.liubs.jareditor.bytestool.asm.instn;

import org.objectweb.asm.tree.FrameNode;

import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/10/21
 */
public class MyInstructionInfo {

    //字节码指令
    private String assemblyCode;

    //行号信息
    private List<LineNumber> markLines ;

    private List<FrameNode> frameNodes ;

    public String getAssemblyCode() {
        return assemblyCode;
    }

    public void setAssemblyCode(String assemblyCode) {
        this.assemblyCode = assemblyCode;
    }

    public List<LineNumber> getMarkLines() {
        return markLines;
    }

    public void setMarkLines(List<LineNumber> markLines) {
        this.markLines = markLines;
    }

    public List<FrameNode> getFrameNodes() {
        return frameNodes;
    }

    public void setFrameNodes(List<FrameNode> frameNodes) {
        this.frameNodes = frameNodes;
    }

    public static class LineNumber {
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

}
