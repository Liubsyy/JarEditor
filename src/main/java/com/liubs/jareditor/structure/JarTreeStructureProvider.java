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
import com.liubs.jareditor.ext.MyJarFileSystem;
import com.liubs.jareditor.ext.JarLikeSupports;
import com.liubs.jareditor.entity.SplitResult;
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
            if(child instanceof PsiFileNode) {
                PsiFileNode psiFileNode = (PsiFileNode)child;
                VirtualFile vf = psiFileNode.getVirtualFile();
                if(null != vf) {

                    //嵌套jar
                    if (isNestedJar(vf)) {
                        String nestedJarBasePath = MyPathUtil.getNestedJarPath(vf.getPath());
                        SplitResult splitResult = JarLikeSupports.split(vf.getPath());
                        String relatePath = splitResult.getParts().get(1);
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

                                        continue;
                                    }
                                }
                            }
                        }
                    }

                    //扩展zip类文件
                    if (isZip(vf)) {
                        // 映射到 xxx.zip!/ 的归档根（directory）
                        VirtualFile root = MyJarFileSystem.getInstance().getJarRootForLocalFile(vf);
                        if (root != null) {
                            PsiDirectory psiDir = psiManager.findDirectory(root);
                            if (psiDir != null) {
                                // 用目录节点替换 zip 文件节点 => 可展开
                                newChildren.add(new NestedJarDirNode(root,project, psiDir, settings));
                                continue;
                            }
                        }
                    }
                }
            }

            newChildren.add(child);
        }

        return newChildren;
    }



    private boolean isNestedJar(VirtualFile file){
        ////包含.jar!/并且当前文件是.jar，那么一定是嵌套jar
        return null != file && file.getPath().matches(JarLikeSupports.MATCHER)
                && "jar".equalsIgnoreCase(file.getExtension());
    }

    //zip类文件扩展
    private boolean isZip(@NotNull VirtualFile vf) {
        String ext = vf.getExtension();
        return ext != null && (ext.equalsIgnoreCase("zip")||ext.equalsIgnoreCase("aar"));
    }


}
