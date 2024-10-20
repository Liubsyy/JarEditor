package com.liubs.jareditor.bytestool.asm;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.Tree;
import com.liubs.jareditor.bytestool.asm.instn.MyGutterIconRenderer;
import com.liubs.jareditor.bytestool.asm.tree.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class ASMEditorPanel extends JPanel {

    // 定义一个唯一的 Key 来标记您的 Highlighter
    private static final Key<Boolean> ASM_LABEL_LINE = Key.create("liubsyy_ASM_LABAL_LINE");


    private Project project;

    private VirtualFile virtualFile;

    private ASMClassModel asmClass;
    private Editor editor;


    public ASMEditorPanel(Project project,VirtualFile virtualFile){
        this.project = project;
        this.virtualFile = virtualFile;

        try {
            byte[] classBytes = VfsUtilCore.loadBytes(virtualFile);
            asmClass = new ASMClassModel(project, classBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setLayout(new BorderLayout());

        // 1. 创建根节点
        BaseTreeNode rootNode = new BaseTreeNode("Root");

        InterfaceTreeCategory interfacesNode = new InterfaceTreeCategory();
        List<String> interfaces = asmClass.getClassNode().interfaces;
        for(String e : interfaces) {
            interfacesNode.add(new InterfaceTreeNode(e));
        }

        FieldTreeCategory fieldsNode = new FieldTreeCategory();
        List<FieldNode> fields = asmClass.getClassNode().fields;
        for(FieldNode fieldNode : fields) {
            fieldsNode.add(new FieldTreeNode(fieldNode));
        }

        MethodTreeCategory methodsNode = new MethodTreeCategory();
        List<MethodNode> methods = asmClass.getClassNode().methods;
        for(MethodNode method : methods) {
            methodsNode.add(new MethodTreeNode(method));
        }

        rootNode.add(interfacesNode);
        rootNode.add(fieldsNode);
        rootNode.add(methodsNode);


        // 3. 创建树，使用 SimpleTree
        Tree tree = new SimpleTree(new DefaultTreeModel(rootNode));
        tree.setRootVisible(false);

        // 4. 设置自定义渲染器
        tree.setCellRenderer(new MyTreeCellRenderer());

        // 5. 使用 JBScrollPane 包装树
        JBScrollPane treeScrollPane = new JBScrollPane(tree);

        // 6. 创建编辑器
        Document document = EditorFactory.getInstance().createDocument("");
        this.editor = EditorFactory.getInstance().createEditor(document, project);


        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(editor.getComponent());

        // 7. 使用 JSplitPane 创建可拖动的分割布局
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, rightPanel);
        splitPane.setDividerLocation(300); // 初始分割线位置 (以像素为单位)
        splitPane.setResizeWeight(0.3); // 初始树面板占 30% 的宽度
        splitPane.setOneTouchExpandable(true); // 在分割线两边增加可展开/折叠按钮

        splitPane.setDividerSize(1);

        // 8. 限制树的最大宽度
        treeScrollPane.setMinimumSize(new Dimension(100, 0));  // 最小宽度 100px
        treeScrollPane.setMaximumSize(new Dimension(400, Integer.MAX_VALUE)); // 最大宽度 400px

        // 9. 将 JSplitPane 添加到主面板中
        add(splitPane, BorderLayout.CENTER);

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                // 获取选中的节点
                BaseTreeNode selectedNode = (BaseTreeNode) tree.getLastSelectedPathComponent();
                if (selectedNode == null) return;

                // 获取选中的节点名称
                String nodeName = selectedNode.getUserObject().toString();

                StringBuilder stringBuilder = new StringBuilder();
                if(selectedNode instanceof MethodTreeNode) {
                    MethodNode methodNode = ((MethodTreeNode) selectedNode).getMethodNode();
                    // 遍历方法的每条指令，并将其打印为文本格式
                    ListIterator<?> instructions = methodNode.instructions.iterator();

                    int lineCount = 0;
                    Map<LabelNode,Integer> labelCount = new HashMap<>();
                    //EditorLineNumber,Label Index,SourceLineNumber
                    List<int[]> markLines = new ArrayList<>();
                    while (instructions.hasNext()) {
                        Object currentInsn = instructions.next();

                        if (currentInsn instanceof InsnNode) {
                            InsnNode insn = (InsnNode) currentInsn;
                            stringBuilder.append(Printer.OPCODES[insn.getOpcode()].toLowerCase());
                        } else if (currentInsn instanceof FrameNode) {
//                            FrameNode frame = (FrameNode) currentInsn;
//                            stringBuilder.append("FrameNode后面待定...");
                            continue;
                        } else if (currentInsn instanceof LabelNode) {
                            if(!instructions.hasNext()){
                                continue;
                            }
                            LabelNode label = (LabelNode) currentInsn;
                            labelCount.computeIfAbsent(label, key -> labelCount.size());
                            markLines.add(new int[]{lineCount,markLines.size(),0 });
                            continue;
                        } else if (currentInsn instanceof IntInsnNode) {
                            IntInsnNode intInsn = (IntInsnNode) currentInsn;
                            stringBuilder.append(Printer.OPCODES[intInsn.getOpcode()].toLowerCase()+" "+intInsn.operand);
                        } else if (currentInsn instanceof LdcInsnNode) {
                            LdcInsnNode ldcInsn = (LdcInsnNode) currentInsn;
                            stringBuilder.append(Printer.OPCODES[ldcInsn.getOpcode()].toLowerCase()+" "+ldcInsn.cst);
                        } else if (currentInsn instanceof VarInsnNode) {
                            VarInsnNode varInsn = (VarInsnNode) currentInsn;
                            stringBuilder.append(Printer.OPCODES[varInsn.getOpcode()].toLowerCase()+" "+varInsn.var);
                        } else if (currentInsn instanceof IincInsnNode) {
                            IincInsnNode iincInsn = (IincInsnNode) currentInsn;
                            stringBuilder.append(Printer.OPCODES[iincInsn.getOpcode()].toLowerCase()+" "+iincInsn.var+","+iincInsn.incr);
                        } else if (currentInsn instanceof JumpInsnNode) {
                            JumpInsnNode jumpInsn = (JumpInsnNode) currentInsn;
                            Integer index = labelCount.computeIfAbsent(jumpInsn.label, key -> labelCount.size());
                            stringBuilder.append(Printer.OPCODES[jumpInsn.getOpcode()].toLowerCase()+" L"+index);
                        } else if (currentInsn instanceof TypeInsnNode) {
                            TypeInsnNode typeInsn = (TypeInsnNode) currentInsn;
                            stringBuilder.append(Printer.OPCODES[typeInsn.getOpcode()].toLowerCase()+" "+typeInsn.desc);
                        } else if (currentInsn instanceof FieldInsnNode) {
                            FieldInsnNode fieldInsn = (FieldInsnNode) currentInsn;
                            stringBuilder.append(Printer.OPCODES[fieldInsn.getOpcode()].toLowerCase() +" "+ fieldInsn.owner+"."+fieldInsn.name+" "+fieldInsn.desc);
                        } else if (currentInsn instanceof LineNumberNode) {
                            LineNumberNode lineNumberInsn = (LineNumberNode) currentInsn;
                            markLines.get(markLines.size()-1)[2] = lineNumberInsn.line;
                            continue;
//                            stringBuilder.append("linenumber "+lineNumberInsn.line+" L"+labelCount.get(lineNumberInsn.start));
                        } else if (currentInsn instanceof MethodInsnNode) {
                            MethodInsnNode methodInsn = (MethodInsnNode) currentInsn;
                            stringBuilder.append(Printer.OPCODES[methodInsn.getOpcode()].toLowerCase()+" "+methodInsn.owner+"."+methodInsn.name+" "+methodInsn.desc);
                        } else if (currentInsn instanceof TableSwitchInsnNode) {
                            TableSwitchInsnNode tableSwitchInsn = (TableSwitchInsnNode) currentInsn;
                            stringBuilder.append("TableSwitchInsnNode后面待定...");
                        } else if (currentInsn instanceof LookupSwitchInsnNode) {
                            LookupSwitchInsnNode lookupSwitchInsn = (LookupSwitchInsnNode) currentInsn;
                            stringBuilder.append("LookupSwitchInsnNode后面待定...");
                        } else if (currentInsn instanceof InvokeDynamicInsnNode) {
                            InvokeDynamicInsnNode invokeDynamicInsn = (InvokeDynamicInsnNode) currentInsn;
                            stringBuilder.append("InvokeDynamicInsnNode后面待定...");
                        } else if (currentInsn instanceof MultiANewArrayInsnNode) {
                            MultiANewArrayInsnNode multiANewArrayInsn = (MultiANewArrayInsnNode) currentInsn;
                            stringBuilder.append(Printer.OPCODES[multiANewArrayInsn.getOpcode()].toLowerCase().toLowerCase()+" "+multiANewArrayInsn.desc+" "+multiANewArrayInsn.dims);
                        }

                        stringBuilder.append("\n");
                        lineCount++;

                        // 使用 Textifier 将字节码指令转化为文本
//                        Textifier textifier = new Textifier();
//                        TraceMethodVisitor traceMethodVisitor = new TraceMethodVisitor(textifier);
                        //((org.objectweb.asm.tree.AbstractInsnNode) insn).accept(traceMethodVisitor);
                        
                        

                    }

                    // 使用 StringWriter 代替 System.out，将内容存储为字符串
//                    StringWriter stringWriter = new StringWriter();
//                    PrintWriter printWriter = new PrintWriter(stringWriter);
//                    textifier.print(printWriter);
//                    printWriter.flush();

                    Document document2 = editor.getDocument();

                    // 使用 WriteCommandAction 修改文件内容
                    WriteCommandAction.runWriteCommandAction(project, () -> {
                        document2.setText(stringBuilder.toString());
                    });

                    // 提交文档更改
                    PsiDocumentManager.getInstance(project).commitDocument(document2);


                    // 收集需要移除的 Highlighter
                    List<RangeHighlighter> toRemove = new ArrayList<>();
                    for (RangeHighlighter highlighter : editor.getMarkupModel().getAllHighlighters()) {
                        if (Boolean.TRUE.equals(highlighter.getUserData(ASM_LABEL_LINE))) {
                            toRemove.add(highlighter);
                        }
                    }
                    // 移除旧的 Highlighter
                    for (RangeHighlighter highlighter : toRemove) {
                        editor.getMarkupModel().removeHighlighter(highlighter);
                    }

                    for(int[] eachLine : markLines) {
                        int lineNumber = eachLine[0];
                        int lineStartOffset = document.getLineStartOffset(lineNumber);
                        int lineEndOffset = document.getLineEndOffset(lineNumber);

                        // 创建 RangeHighlighter
                        RangeHighlighter highlighter = editor.getMarkupModel().addRangeHighlighter(
                                lineStartOffset,
                                lineEndOffset,
                                HighlighterLayer.FIRST,
                                null,
                                HighlighterTargetArea.LINES_IN_RANGE
                        );

                        // 设置自定义的 GutterIconRenderer
                        highlighter.setGutterIconRenderer(new MyGutterIconRenderer(eachLine[1],lineNumber,eachLine[2]));
                        highlighter.putUserData(ASM_LABEL_LINE, Boolean.TRUE);
                    }

                }
            }
        });
    }


}
