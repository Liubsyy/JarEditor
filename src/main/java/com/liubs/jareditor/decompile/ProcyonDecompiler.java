package com.liubs.jareditor.decompile;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.strobel.Procyon;
import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

import java.io.IOException;
import java.io.StringWriter;
import java.util.jar.JarFile;

/**
 * Procyon反编译器
 * @author Liubsyy
 * @date 2024/10/8
 */
public class ProcyonDecompiler implements IDecompiler{



    public static String decompileClass(String jarPath,String className, byte[] classBytes) {
        // 创建一个 StringWriter 来保存反编译结果
        StringWriter stringWriter = new StringWriter();
        PlainTextOutput output = new PlainTextOutput(stringWriter);

        // 配置反编译设置
        DecompilerSettings settings = DecompilerSettings.javaDefaults();
        settings.setForceExplicitImports(true);
        settings.setOutputFileHeaderText("\nDecompiled by Procyon v" + Procyon.version() + "\n");


        // 创建 CompositeTypeLoader，首先尝试从内存加载，否则从默认加载器加载
        ITypeLoader typeLoader = null;
        try(JarFile jarFile = new JarFile(jarPath)) {
            typeLoader = new CompositeTypeLoader(
                    new ArrayTypeLoader(classBytes),
                    new JarTypeLoader(jarFile),
                    new InputTypeLoader() // 默认类型加载器
            );

            // 设置自定义的 TypeLoader
            settings.setTypeLoader(typeLoader);

            // 使用 MetadataSystem 来解析类型
            MetadataSystem metadataSystem = new MetadataSystem(typeLoader);
            TypeReference type = metadataSystem.lookupType(className);

            if (type == null) {
                throw new IllegalArgumentException("无法找到类型：" + className);
            }

            TypeDefinition resolvedType = null;
            try {
                resolvedType = type.resolve();
            } catch (Exception e) {
                throw new RuntimeException("无法解析类型：" + className, e);
            }

            // 创建 DecompilationOptions 并设置反编译设置
            DecompilationOptions options = new DecompilationOptions();
            options.setSettings(settings);
            options.setFullDecompilation(true);

            // 反编译类型并输出到 StringWriter
            settings.getLanguage().decompileType(resolvedType, output, options);

            // 返回反编译后的源代码
            return stringWriter.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return "";
    }

    @Override
    public String decompile(Project project, VirtualFile virtualFile) {
        String[] path = virtualFile.getPath().split(".jar!/");
        try {
            return decompileClass(path[0]+".jar",path[1].replace(".class", ""), VfsUtilCore.loadBytes(virtualFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
