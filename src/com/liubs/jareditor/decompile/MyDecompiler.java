package com.liubs.jareditor.decompile;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PsiErrorElementUtil;
import com.liubs.jareditor.persistent.SDKSettingStorage;

import java.util.Objects;

/**
 * @author Liubsyy
 * @date 2024/6/1
 */
public class MyDecompiler {

    public static String getDecompiledText(Project project, VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null && !PsiErrorElementUtil.hasErrors(project, file)) {
            if(Objects.equals(file.getExtension(), "class")){
                boolean useDecompiler;
                if("java".equalsIgnoreCase(psiFile.getLanguage().getDisplayName())) {
                    if(SDKSettingStorage.getInstance().getDecompiledTool() == DecompiledEnum.FERNFLOWER.value) {
                        //如果是默认反编译，则不需要显式编译，psiFile.getText()就是默认IDEA自带反编译的内容
                        useDecompiler = false;
                    }else {
                        useDecompiler = true;
                    }
                }else {
                    useDecompiler = true;
                }

                if(useDecompiler) {
                    return DecompiledEnum.findByValue(SDKSettingStorage.getInstance().getDecompiledTool())
                            .decompiler.decompile(project,file);
                }else {
                    return psiFile.getText();
                }
            }else {
                return psiFile.getText();
            }
        }
        return "";
    }

    
}
