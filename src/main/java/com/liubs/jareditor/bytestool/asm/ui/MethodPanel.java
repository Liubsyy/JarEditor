package com.liubs.jareditor.bytestool.asm.ui;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.liubs.jareditor.bytestool.asm.entity.MyInstructionInfo;
import com.liubs.jareditor.bytestool.asm.tree.MethodTreeNode;
import com.liubs.jareditor.bytestool.asm.constant.AccessConstant;
import org.objectweb.asm.tree.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;


/**
 * @author Liubsyy
 * @date 2024/10/21
 */
public class MethodPanel extends JPanel implements IPanelRefresh<MethodTreeNode> {

    private Project project;
    private Editor editor;

    //access
    private JLabel access;

    //name
    private JLabel name;

    //desc
    private JLabel desc;

    //Exception Table
    private DefaultTableModel exceptionTable;

    //LocalVariable
    private DefaultTableModel localVariableTable;

    //Params
    private DefaultTableModel paramsTable;




    public MethodPanel(Project project) {
        this.project = project;

        Document document = EditorFactory.getInstance().createDocument("");
        this.editor = EditorFactory.getInstance().createEditor(document, project);

        setLayout(new BorderLayout());

        access = new JLabel();
        name = new JLabel();
        desc = new JLabel();

        JPanel baseInfo = FormBuilder.createFormBuilder()
                .setVerticalGap(8)
                .addLabeledComponent("Access : ", access)
                .addLabeledComponent("Name : ", name)
                .addLabeledComponent("Desc : ", desc)
                .getPanel();

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        baseInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(etchedBorder, "Method Info"),
                JBUI.Borders.empty(8)));


        exceptionTable = new DefaultTableModel(new Object[0][0],
                new String[]{"Start", "End", "Handler","Type"});

        localVariableTable = new DefaultTableModel(new Object[0][0],
                new String[]{"Index", "Start", "End","Name","Desc"});

        paramsTable = new DefaultTableModel(new Object[0][0],
                new String[]{"Name", "Access"});

        //tabbed pane
        JBTabbedPane tabbedPane = new JBTabbedPane();
        tabbedPane.add("Code",editor.getComponent());
        tabbedPane.add("Exception",new JBScrollPane(new JBTable(exceptionTable)));
        tabbedPane.add("LocalVariable",new JBScrollPane(new JBTable(localVariableTable)));
        tabbedPane.add("Params",new JBScrollPane(new JBTable(paramsTable)));

        this.add(baseInfo,BorderLayout.NORTH);
        this.add(tabbedPane,BorderLayout.CENTER);
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

        MethodNode methodNode = treeNode.getMethodNode();

        /* 1.刷新基础信息 */
        access.setText(String.format("0x%04x(%s)", methodNode.access, String.join(" ",AccessConstant.getMethodFlagNames(methodNode.access))));
        name.setText(methodNode.name);
        desc.setText(methodNode.desc);


        /* 2.刷新Code编辑器 */
        //获取code指令信息
        MyInstructionInfo instructionInfo = treeNode.getInstructionInfo();

        //将字节码指令写入到编辑器
        writeEditorContent(instructionInfo.getAssemblyCode());

        //行号标示高亮
        MyLineGutterRenderer.markLineLighter(editor,instructionInfo.getMarkLines());

        /* 3.刷新Exception Table */
        Map<LabelNode, Integer> labelIndexMap = instructionInfo.getLabelIndexMap();
        //TODO TryCatchBlockNode还有visibleTypeAnnotations和invisibleTypeAnnotations
        List<TryCatchBlockNode> tryCatchBlockNodes = methodNode.tryCatchBlocks;
        if(null != tryCatchBlockNodes){
            Object[][] newTableData = new Object[tryCatchBlockNodes.size()][exceptionTable.getColumnCount()];
            for(int i = 0,len=tryCatchBlockNodes.size() ; i<len ; i++){
                TryCatchBlockNode n = tryCatchBlockNodes.get(i);
                Integer start = labelIndexMap.get(n.start);
                Integer end = labelIndexMap.get(n.end);
                Integer handler = labelIndexMap.get(n.handler);

                newTableData[i][0] = "L"+start;
                newTableData[i][1] = "L"+end;
                newTableData[i][2] = "L"+handler;
                newTableData[i][3] = n.type;
            }
            fillTable(exceptionTable,newTableData);
        }

        /* 4.刷新LocalVariable */
        List<LocalVariableNode> localVariables = methodNode.localVariables;
        if(null != localVariables){
            Object[][] newTableData = new Object[localVariables.size()][localVariableTable.getColumnCount()];
            for(int i = 0,len=localVariables.size() ; i<len ; i++){
                LocalVariableNode n = localVariables.get(i);
                Integer start = labelIndexMap.get(n.start);
                Integer end = labelIndexMap.get(n.end);

                newTableData[i][0] = n.index;
                newTableData[i][1] = "L"+start;
                newTableData[i][2] = "L"+end;
                newTableData[i][3] = n.name;
                newTableData[i][4] = n.desc;
            }
            fillTable(localVariableTable,newTableData);
        }

        /* 5.刷新Params */
        List<ParameterNode> parameters = methodNode.parameters;
        if(null != parameters){
            Object[][] newTableData = new Object[parameters.size()][paramsTable.getColumnCount()];
            for(int i = 0,len=parameters.size() ; i<len ; i++){
                ParameterNode n = parameters.get(i);
                newTableData[i][0] = n.name;
                newTableData[i][1] = n.access;
            }
            fillTable(paramsTable,newTableData);
        }


    }


    private static void fillTable(DefaultTableModel tableModel, Object[][] newTableData){
        tableModel.setRowCount(0);
        for (Object[] row : newTableData) {
            tableModel.addRow(row);
        }
    }


}
