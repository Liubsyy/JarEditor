package com.liubs.jareditor.decompile;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.lang.reflect.Method;

/**
 * IDEA自带反编译器
 * @author Liubsyy
 * @date 2024/10/8
 */
public class IdeaDecompiler implements IDecompiler{

    private static ClassLoader pluginClassLoader;
    private static Object decompiler;
    private static Method decompileMethod;

    @Override
    public String decompile(Project project, VirtualFile virtualFile) {
        return decompileText(virtualFile);
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
}
