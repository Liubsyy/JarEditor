package com.liubs.jareditor.structure;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.liubs.jareditor.util.MyPathUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * jar文件树扩展，主要兼容嵌套jar的文件树展开
 * @author Liubsyy
 * @date 2024/10/11
 */
public class JarTreeStructureProvider implements TreeStructureProvider {

    @NotNull
    @Override
    public Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent,
                                                  @NotNull Collection<AbstractTreeNode<?>> children,
                                                  @NotNull ViewSettings settings) {
        Project project = parent.getProject();
        if(project == null) {
            return children;
        }

        NestedJarHolder nestedJarHolder = NestedJarHolder.getInstance(project);


        List<AbstractTreeNode<?>> newChildren = new ArrayList<>();
        final PsiManager psiManager = PsiManager.getInstance(project);
        for (AbstractTreeNode<?> child : children) {
            boolean isEffectNestedJar = false;

            if(child instanceof PsiFileNode) {
                PsiFileNode psiFileNode = (PsiFileNode)child;
                VirtualFile file = psiFileNode.getVirtualFile();

                if (isNestedJar(file)) {
                    String nestedJarBasePath = MyPathUtil.getNestedJarPath(file.getPath());
                    String relatePath = file.getPath().substring(file.getPath().indexOf(".jar!") + ".jar!".length());
                    if(null != nestedJarBasePath) {
                        Path destinationPath = Paths.get(nestedJarBasePath, relatePath);

                        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(destinationPath.toString());
                        if(null != virtualFile) {
                            VirtualFile nestedJarVirtualFile = JarFileSystem.getInstance().getJarRootForLocalFile(virtualFile);
                            if(null != nestedJarVirtualFile) {
                                final PsiDirectory psiDir = psiManager.findDirectory(nestedJarVirtualFile);
                                if(null != psiDir) {
                                    newChildren.add(new NestedJarDirNode(nestedJarVirtualFile,project,psiDir,settings));
                                    nestedJarHolder.addExpandPath(destinationPath.toString());

                                    isEffectNestedJar = true;
                                }
                            }
                        }
                    }
                }
            }
            if(!isEffectNestedJar) {
                newChildren.add(child);
            }
        }

        return newChildren;
    }

    private boolean isNestedJar(VirtualFile file){
        ////包含.jar!/并且当前文件是.jar，那么一定是嵌套jar
        return null != file && file.getPath().contains(".jar!/")
                && "jar".equalsIgnoreCase(file.getExtension());
    }


}
