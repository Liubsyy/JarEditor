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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Procyon反编译器
 * @author Liubsyy
 * @date 2024/10/8
 */
public class ProcyonDecompiler implements IDecompiler{



    public static String decompileClass(String className, byte[] classBytes) {
        // 创建一个 StringWriter 来保存反编译结果
        StringWriter stringWriter = new StringWriter();
        PlainTextOutput output = new PlainTextOutput(stringWriter);

        // 配置反编译设置
        DecompilerSettings settings = DecompilerSettings.javaDefaults();
        settings.setForceExplicitImports(true);
        settings.setOutputFileHeaderText("\nDecompiled by Procyon v" + Procyon.version() + "\n");


        // 创建一个 Map 来存储类的字节码
        Map<String, byte[]> classBytesMap = new HashMap<>();
        // 将类名转换为内部名（用斜杠代替点号）
        String internalName = className.replace('.', '/');
        classBytesMap.put(internalName, classBytes);

        // 实现自定义的 ITypeLoader，从内存中加载类字节码
        ITypeLoader memoryTypeLoader = new ITypeLoader() {
            @Override
            public boolean tryLoadType(String typeName, Buffer buffer) {
                // 规范化类型名称
                String normalizedTypeName = typeName.replace('.', '/');
                byte[] bytes = classBytesMap.get(normalizedTypeName);

                if (bytes != null) {
                    buffer.putByteArray(bytes, 0, bytes.length);
                    // 重置缓冲区位置
                    buffer.position(0);
                    return true;
                }
                return false;
            }
        };

        // 创建 CompositeTypeLoader，首先尝试从内存加载，否则从默认加载器加载
        ITypeLoader typeLoader = new CompositeTypeLoader(
                memoryTypeLoader,
                new InputTypeLoader() // 默认类型加载器
        );

        // 设置自定义的 TypeLoader
        settings.setTypeLoader(typeLoader);

        // 使用 MetadataSystem 来解析类型
        MetadataSystem metadataSystem = new MetadataSystem(typeLoader);
        TypeReference type = metadataSystem.lookupType(internalName);

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
    }

    @Override
    public String decompile(Project project, VirtualFile virtualFile) {
        String path = virtualFile.getPath().split(".jar!/")[1];
        try {
            return decompileClass(path.replace(".class", "").replace("/", "."), VfsUtilCore.loadBytes(virtualFile));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
