package com.liubs.jareditor.bytestool.asm.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.liubs.jareditor.bytestool.asm.aggregate.MyAssemblyClass;
import com.liubs.jareditor.bytestool.asm.tree.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.io.IOException;

/**
 * @author Liubsyy
 * @date 2024/10/19
 */
public class ASMEditorPanel extends JPanel implements TreeSelectionListener{

    private Project project;
    private VirtualFile virtualFile;

    private MyAssemblyClass asmClassService;

    private MyTree myTree;
    private ContentPanel rightPanel;

    public ASMEditorPanel(Project project,VirtualFile virtualFile){
        this.project = project;
        this.virtualFile = virtualFile;

        try {
            byte[] classBytes = VfsUtilCore.loadBytes(virtualFile);
            asmClassService = new MyAssemblyClass(classBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setLayout(new BorderLayout());


        // 创建树
        myTree = new MyTree();
        BaseTreeNode rootNode = myTree.initNodes(asmClassService);
        myTree.addTreeSelectionListener(this);
        JBScrollPane treeScrollPane = new JBScrollPane(myTree);

        rightPanel = new ContentPanel(project);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, rightPanel);
        splitPane.setDividerLocation(300); // 初始分割线位置 (以像素为单位)
        splitPane.setResizeWeight(0.3); // 初始树面板占 30% 的宽度
//        splitPane.setOneTouchExpandable(true); // 在分割线两边增加可展开/折叠按钮
        splitPane.setDividerSize(1);
        splitPane.setUI(new BasicSplitPaneUI() {
            @Override
            public BasicSplitPaneDivider createDefaultDivider() {
                return new BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        g.setColor(JBColor.LIGHT_GRAY); // 设置分割线的颜色
                        g.fillRect(0, 0, getWidth(), getHeight());
                        super.paint(g);
                    }
                };
            }
        });

        //限制树的最大宽度
        treeScrollPane.setMinimumSize(new Dimension(100, 0));  // 最小宽度 100px
        treeScrollPane.setMaximumSize(new Dimension(400, Integer.MAX_VALUE)); // 最大宽度 400px

        this.add(splitPane, BorderLayout.CENTER);


        //选中ClassInfo
        try{
            myTree.selectNode((BaseTreeNode)rootNode.getFirstChild());
        }catch (Throwable ex){}
    }




    @Override
    public void valueChanged(TreeSelectionEvent e) {
        // 获取选中的节点
        BaseTreeNode selectedNode = (BaseTreeNode) myTree.getLastSelectedPathComponent();
        if (selectedNode == null) return;

        // 获取选中的节点名称
        //String nodeName = selectedNode.getUserObject().toString();

        rightPanel.refresh(selectedNode);
    }

    public void dispose() {
        if(null != rightPanel) {
            rightPanel.dispose();
        }

    }
}
