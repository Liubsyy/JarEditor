package com.liubs.jareditor.bytestool.asm.ui;

import com.intellij.openapi.Disposable;
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
import com.liubs.jareditor.bytestool.asm.entity.MyLineNumber;
import com.liubs.jareditor.bytestool.asm.tree.MethodTreeNode;
import com.liubs.jareditor.bytestool.asm.constant.AccessConstant;
import org.objectweb.asm.tree.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


/**
 * @author Liubsyy
 * @date 2024/10/21
 */
public class MethodPanel extends JPanel implements IPanelRefresh<MethodTreeNode>, Disposable {

    private Project project;
    private Editor editor;

    //access
    private JLabel access;

    //name
    private JLabel name;

    //desc
    private JLabel desc;

    //exception declared
    private JLabel exceptionDeclared;

    private JLabel maxStack;
    private JLabel maxLocals;

    //LineNumber
    private DefaultTableModel lineNumberTable;

    //LocalVariable
    private DefaultTableModel localVariableTable;

    //Exception Table
    private DefaultTableModel exceptionTable;


    //Params
    private DefaultTableModel paramsTable;

    private DefaultTableModel visibleAnnotations;
    private DefaultTableModel invisibleAnnotations;




    public MethodPanel(Project project) {
        this.project = project;

        Document document = EditorFactory.getInstance().createDocument("");
        this.editor = EditorFactory.getInstance().createEditor(document, project);

        setLayout(new BorderLayout());

        access = new JLabel();
        name = new JLabel();
        desc = new JLabel();
        exceptionDeclared = new JLabel();
        maxStack = new JLabel();
        maxLocals = new JLabel();

        JPanel baseInfo = FormBuilder.createFormBuilder()
                .setVerticalGap(8)
                .addLabeledComponent("Access : ", access)
                .addLabeledComponent("Name : ", name)
                .addLabeledComponent("Desc : ", desc)
                .addLabeledComponent("throws : ", exceptionDeclared)
                .getPanel();

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        baseInfo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(etchedBorder, "Method Info"),
                JBUI.Borders.empty(8)));


        exceptionTable = new DefaultTableModel(new Object[0][0],
                new String[]{"Start", "End", "Jump(Exception)","Type"});

        lineNumberTable = new DefaultTableModel(new Object[0][0],
                new String[]{"Label", "Line Number"});

        localVariableTable = new DefaultTableModel(new Object[0][0],
                new String[]{"Index", "Start", "End","Name","Desc"});

        paramsTable = new DefaultTableModel(new Object[0][0], new String[]{"Name", "Access"});
        visibleAnnotations = new DefaultTableModel(new Object[0][0], new String[]{"Desc", "Value"});
        invisibleAnnotations = new DefaultTableModel(new Object[0][0], new String[]{"Desc", "Value"});

        JPanel otherAttributePanel = FormBuilder.createFormBuilder()
                .setVerticalGap(8)
                .addLabeledComponent("Max Stack : ", maxStack)
                .addLabeledComponent("Max Locals : ", maxLocals)
                .addLabeledComponent("Parameters : ", new JBScrollPane(new JBTable(paramsTable)))
                .addLabeledComponent("Visible Annotations : ", new JBScrollPane(new JBTable(visibleAnnotations)))
                .addLabeledComponent("Invisible Annotations : ", new JBScrollPane(new JBTable(invisibleAnnotations)))
                .getPanel();

        //tabbed pane
        JBTabbedPane tabbedPane = new JBTabbedPane();
        tabbedPane.add("Code",editor.getComponent());
        tabbedPane.add("LocalVariable",new JBScrollPane(new JBTable(localVariableTable)));
        tabbedPane.add("Exception",new JBScrollPane(new JBTable(exceptionTable)));
        tabbedPane.add("LineNumber",new JBScrollPane(new JBTable(lineNumberTable)));
        tabbedPane.add("Others",otherAttributePanel);

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

        // 刷新基础信息和杂项信息
        access.setText(String.format("0x%04x(%s)", methodNode.access, String.join(" ",AccessConstant.getMethodFlagNames(methodNode.access))));
        name.setText(methodNode.name);
        desc.setText(methodNode.desc);
        exceptionDeclared.setText(String.join(",",methodNode.exceptions));
        maxStack.setText(String.valueOf(methodNode.maxStack));
        maxLocals.setText(String.valueOf(methodNode.maxLocals));

        //获取code指令信息
        MyInstructionInfo instructionInfo = treeNode.getInstructionInfo();

        //将字节码指令写入到编辑器
        writeEditorContent(instructionInfo.getAssemblyCode());
        Map<LabelNode, Integer> labelIndexMap = instructionInfo.getLabelIndexMap();

        //编辑器写入行号
        List<MyLineNumber> markLines = instructionInfo.getMarkLines();

        //行号标示高亮
        MyLineGutterRenderer.markLineLighter(editor,markLines);

        //行号表
        markLines = markLines.stream().filter(c->c.getLineSource()>=0).collect(Collectors.toList());
        fillTables(lineNumberTable,markLines,(n,dataRow)-> {
            dataRow[0] = "L"+n.getLabelIndex();
            dataRow[1] = n.getLineSource();
        });


        // 刷新LocalVariable
        fillTables(localVariableTable,methodNode.localVariables,(n,dataRow)-> {
            Integer start = labelIndexMap.get(n.start);
            Integer end = labelIndexMap.get(n.end);

            dataRow[0] = n.index;
            dataRow[1] = "L"+start;
            dataRow[2] = "L"+end;
            dataRow[3] = n.name;
            dataRow[4] = n.desc;
        });

        // 刷新Exception Table
        //TODO TryCatchBlockNode还有visibleTypeAnnotations和invisibleTypeAnnotations
        fillTables(exceptionTable,methodNode.tryCatchBlocks,(n,dataRow)-> {
            Integer start = labelIndexMap.get(n.start);
            Integer end = labelIndexMap.get(n.end);
            Integer handler = labelIndexMap.get(n.handler);

            dataRow[0] = "L"+start;
            dataRow[1] = "L"+end;
            dataRow[2] = "L"+handler;
            dataRow[3] = n.type;
        });


        // 刷新Parameters
        fillTables(paramsTable,methodNode.parameters,(n,dataRow)-> {
            dataRow[0] = n.name;
            dataRow[1] = n.access;
        });

        //刷新 visibleAnnotations
        fillTables(visibleAnnotations, methodNode.visibleAnnotations,(n,dataRow)->{
            dataRow[0] = n.desc;
            dataRow[1] = null == n.values ? null : n.values.stream().map(Object::toString).collect(Collectors.joining(","));
        });
        fillTables(invisibleAnnotations, methodNode.invisibleAnnotations,(n,dataRow)->{
            dataRow[0] = n.desc;
            dataRow[1] = null == n.values ? null : n.values.stream().map(Object::toString).collect(Collectors.joining(","));
        });


    }
    
    
    public static <T> void fillTables(DefaultTableModel tableModel, List<T> nodes, BiConsumer<T,Object[]> fillDataHandler) {
        if(null == nodes) {
            return;
        }
        Object[][] newTableData = new Object[nodes.size()][tableModel.getColumnCount()];
        for(int i = 0,len=nodes.size() ; i<len ; i++){
            T n = nodes.get(i);
            fillDataHandler.accept(n,newTableData[i]);
        }
        tableModel.setRowCount(0);
        for (Object[] row : newTableData) {
            tableModel.addRow(row);
        }
    }


    @Override
    public void dispose() {
        try{
            EditorFactory.getInstance().releaseEditor(editor);
        }catch (Throwable e) {}

    }
}
