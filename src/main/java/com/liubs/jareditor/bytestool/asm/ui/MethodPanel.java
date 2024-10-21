package com.liubs.jareditor.bytestool.asm.ui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.liubs.jareditor.bytestool.asm.instn.MyInstructionInfo;
import com.liubs.jareditor.bytestool.asm.tree.MethodTreeNode;

import javax.swing.*;
import java.awt.*;


/**
 * @author Liubsyy
 * @date 2024/10/21
 */
public class MethodPanel extends JPanel implements IPanelRefresh<MethodTreeNode> {

    private Project project;
    private Editor editor;

    public MethodPanel(Project project) {
        this.project = project;

        Document document = EditorFactory.getInstance().createDocument("");
        this.editor = EditorFactory.getInstance().createEditor(document, project);

        setLayout(new BorderLayout());
        this.add(editor.getComponent());
    }


    public void writeEditorContent(String content){
        Document document = editor.getDocument();

        // 使用 WriteCommandAction 修改文件内容
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.setText(content);
        });

        // 提交文档更改
        PsiDocumentManager.getInstance(project).commitDocument(document);
    }

    @Override
    public void refresh(MethodTreeNode treeNode) {

        //获取code指令信息
        MyInstructionInfo instructionInfo = treeNode.getInstructionInfo();

        //将字节码指令写入到编辑器
        writeEditorContent(instructionInfo.getAssemblyCode());

        //行号标示高亮
        MyLineGutterRenderer.markLineLighter(editor,instructionInfo.getMarkLines());
    }


}
