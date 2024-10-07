package com.liubs.jareditor.decompile;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.PsiErrorElementUtil;
import com.liubs.jareditor.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author Liubsyy
 * @date 2024/6/1
 */
public class MyDecompiler {
    private static ClassLoader pluginClassLoader;
    private static Object decompiler;
    private static Method decompileMethod;

    public static String getDecompiledText(Project project, VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null && !PsiErrorElementUtil.hasErrors(project, file)) {
            if(Objects.equals(file.getExtension(), "class")
                    && !"java".equalsIgnoreCase(psiFile.getLanguage().getDisplayName())) {
                String decompileText = MyDecompiler.decompileText(file);
                return StringUtils.isEmpty(decompileText) ? psiFile.getText() : decompileText;
            }
            return psiFile.getText(); //default decompiled text;
        }
        return "";
    }


    private static ClassLoader getPluginClassLoader(){
        try{
            IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId("org.jetbrains.java.decompiler"));
            if(null == plugin) {
                return null;
            }
            return plugin.getPluginClassLoader();
        }catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static String decompileText(VirtualFile file) {
        if(null == pluginClassLoader) {
            pluginClassLoader = getPluginClassLoader();
            if(null == pluginClassLoader) {
                return "";
            }
        }

        // org.jetbrains.java.decompiler.IdeaDecompiler ideaDecompiler = new org.jetbrains.java.decompiler.IdeaDecompiler();
        // String text =  (String) ideaDecompiler.decompile(file);
        try {
            if(null == decompiler) {
                Class<?> decompilerCls = pluginClassLoader.loadClass("org.jetbrains.java.decompiler.IdeaDecompiler");
                decompiler = decompilerCls.getConstructor().newInstance();
            }
            if(null == decompileMethod) {
                decompileMethod = decompiler.getClass().getDeclaredMethod("decompile", VirtualFile.class);
                decompileMethod.setAccessible(true);
            }
            return (String)decompileMethod.invoke(decompiler, file);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
    
}
