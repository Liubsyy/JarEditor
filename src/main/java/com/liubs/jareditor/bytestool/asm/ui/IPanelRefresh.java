package com.liubs.jareditor.bytestool.asm.ui;

import com.liubs.jareditor.bytestool.asm.tree.BaseTreeNode;

/**
 * @author Liubsyy
 * @date 2024/10/21
 */
public interface IPanelRefresh<T extends BaseTreeNode> {
    void refresh(T treeNode);
}
