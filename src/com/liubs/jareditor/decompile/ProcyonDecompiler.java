package com.liubs.jareditor.decompile;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.liubs.jareditor.constant.PathConstant;
import com.liubs.jareditor.util.JavaFileUtil;
import com.strobel.Procyon;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarFile;

/**
 * Procyon反编译器
 * @author Liubsyy
 * @date 2024/10/8
 */
public class ProcyonDecompiler implements IDecompiler{


    private static String decompileClass(String className, ITypeLoader typeLoader){
        StringWriter stringWriter = new StringWriter();
        PlainTextOutput output = new PlainTextOutput(stringWriter);
        // 配置反编译设置
        DecompilerSettings settings = DecompilerSettings.javaDefaults();
        settings.setForceExplicitImports(true);
        settings.setOutputFileHeaderText("\nDecompiled by Procyon v" + Procyon.version() + "\n");

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
    }


    @Override
    public String decompile(Project project, VirtualFile virtualFile) {

        JarFile jarFile = null;
        try {
            String className;
            ITypeLoader typeLoader;
            if(virtualFile.getPath().contains(".jar!/")) {
                String[] split = virtualFile.getPath().split(".jar!/");
                String jarPath = split[0]+".jar";
                className = split[1].replace(".class", "");
                jarFile = new JarFile(jarPath);
                typeLoader = new CompositeTypeLoader(new JarTypeLoader(jarFile),new ArrayTypeLoader(VfsUtilCore.loadBytes(virtualFile)));
            }else {
                className = virtualFile.getPath().split(PathConstant.TEMP_SUFFIX+"/"+ PathConstant.JAR_EDIT_CLASS_PATH+"/")[1].replace(".class", "");
                List<String> fullClassFiles = JavaFileUtil.getFullClassFiles(virtualFile.getPath());
                ITypeLoader[] typeLoaders = new ITypeLoader[fullClassFiles.size()];
                for(int i = 0;i<fullClassFiles.size();i++){
                    try {
                        byte[] bytes = Files.readAllBytes(Paths.get(fullClassFiles.get(i)));
                        typeLoaders[i] = new ArrayTypeLoader(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                typeLoader = new CompositeTypeLoader(typeLoaders);
            }

            return decompileClass(className, typeLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(null != jarFile) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "";
    }

}
