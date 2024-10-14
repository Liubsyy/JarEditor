package com.liubs.jareditor.structure;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiDirectoryNode;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Liubsyy
 * @date 2024/10/13
 */
public class NestedJarDirNode extends PsiDirectoryNode {

    private VirtualFile entryFile;
    private PsiManager psiManager;

    public NestedJarDirNode(VirtualFile entryFile, Project project, @NotNull PsiDirectory value, ViewSettings viewSettings) {
        super(project, value, viewSettings);
        this.entryFile = entryFile;
        this.psiManager = PsiManager.getInstance(project);
    }

    @Override
    public Collection<AbstractTreeNode<?>> getChildrenImpl() {
        VirtualFile[] subChildren = entryFile.getChildren();
        Collection<AbstractTreeNode<?>> children = new ArrayList<>(subChildren.length);
        for(VirtualFile sub : subChildren) {
            if(sub.isDirectory()) {
                final PsiDirectory psiDir = psiManager.findDirectory(sub);
                if(null != psiDir) {
                    children.add(new NestedJarDirNode(sub,getProject(),psiDir,getSettings()));
                }
            }else {
                final PsiFile psiFile = psiManager.findFile(sub);
                if (psiFile != null) {
                    children.add(new PsiFileNode(getProject(), psiFile, getSettings()));
                }
            }
        }
        return children;
    }
}
