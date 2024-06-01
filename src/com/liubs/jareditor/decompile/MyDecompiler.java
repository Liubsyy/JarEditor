package com.liubs.jareditor.decompile;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.vfs.VirtualFile;

import java.lang.reflect.Method;

/**
 * @author Liubsyy
 * @date 2024/6/1
 */
public class MyDecompiler {
    private static ClassLoader pluginClassLoader;
    private static Object decompiler;
    private static Method decompileMethod;

    private static ClassLoader getPluginClassLoader(){
        try{
            IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(PluginId.getId("org.jetbrains.java.decompiler"));
            return ((IdeaPluginDescriptorImpl)plugin).getPluginClassLoader();
        }catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Decompile file anyway
     * @param file
     * @return
     */
    public static String decompileText(VirtualFile file) {
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
