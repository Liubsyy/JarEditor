package com.liubs.jareditor.decompile;

import com.liubs.jareditor.entity.SplitResult;
import com.liubs.jareditor.ext.JarLikeSupports;
import com.liubs.jareditor.util.ExceptionUtil;
import com.liubs.jareditor.util.JarUtil;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import jadx.api.impl.NoOpCodeCache;
import jadx.api.impl.SimpleCodeWriter;
import jadx.api.plugins.JadxPlugin;
import jadx.api.plugins.loader.JadxPluginLoader;
import jadx.plugins.input.java.JavaClassReader;
import jadx.plugins.input.java.JavaInputLoader;
import jadx.plugins.input.java.JavaInputPlugin;

/**
 * jadx反编译器
 * @author Liubsyy
 * @date 2026/2/12
 */
public class JadXDecompiler implements IDecompiler{
    @Override
    public String decompile(Project project, VirtualFile virtualFile) {
        try{
            SplitResult splitResult = JarLikeSupports.split(virtualFile.getPath());
            if(splitResult.getParts().size() <= 1) {
                return "";
            }else {
                String jarPath = splitResult.filePath0();
                String className = splitResult.getParts().get(1);
                if(className.endsWith(".class")) {
                    className = className.substring(0,className.lastIndexOf(".class"));
                }
                className = className.replace("/", ".");


                //这种方式加载大jar时很慢甚至卡死，改用下面只加载部分class的方式
//                return decompileClassFromJar(jarPath,className);

                Map<String, byte[]> classBytes = JarUtil.readJarClasses(jarPath, className);
                return decompileClassFromJar(classBytes,className);
            }
        }catch (Exception e){
            e.printStackTrace();
            return "decompile error : "+ExceptionUtil.getExceptionTracing(e);
        }
    }


    public static String decompileClassFromJar(String jarPath, String className) {

        JadxArgs args = new JadxArgs();
        args.setInputFile(new File(jarPath));
        args.setSkipResources(true);
        args.setSkipSources(true);
        args.setSkipFilesSave(true);
        args.setSkipXmlPrettyPrint(true);
        args.setCodeWriterProvider(SimpleCodeWriter::new);
        args.setCodeCache(new NoOpCodeCache());

        //这里设置classloader，否则加载不到jadx-java-input插件模块
        //jadx-java-input是反编译java的模块
        args.setPluginLoader(new JadxPluginLoader() {
            @Override
            public List<JadxPlugin> load() {
                List<JadxPlugin> list = new ArrayList<>();
                ServiceLoader<JadxPlugin> plugins = ServiceLoader.load(JadxPlugin.class,
                        JadXDecompiler.class.getClassLoader());
                for (JadxPlugin plugin : plugins) {
                    list.add(plugin);
                }
                return list;
            }

            @Override
            public void close() throws IOException {}
        });

        try (JadxDecompiler jadx = new JadxDecompiler(args)) {
            jadx.load();

            List<JavaClass> classes = jadx.getClasses();

            // 1) 优先按 fullName 精确匹配（通常就是你传的 "com.example.Foo"）
            Optional<JavaClass> hit = classes.stream()
                    .filter(c -> className.equals(c.getFullName()))
                    .findFirst();

            // 2) 有些情况下 fullName 可能不完全一致，再尝试“后缀匹配”
            if (hit.isEmpty()) {
                String suffix = "." + className;
                hit = classes.stream()
                        .filter(c -> {
                            String fn = c.getFullName();
                            return fn != null && (fn.equals(className) || fn.endsWith(suffix));
                        })
                        .findFirst();
            }

            JavaClass jc = hit.orElseThrow(() ->
                    new IllegalArgumentException("Class not found in jar: " + className));

            //直接拿源码字符串（不写文件）
            String code = jc.getCode();
            return code != null ? code : "";
        }
    }


    public static String decompileClassFromJar(Map<String, byte[]> classBytes, String className) {

        JadxArgs args = new JadxArgs();

        //这里设置classloader，否则加载不到jadx-java-input插件模块
        //jadx-java-input是反编译java的模块
        args.setPluginLoader(new JadxPluginLoader() {
            @Override
            public List<JadxPlugin> load() {
                List<JadxPlugin> list = new ArrayList<>();
                ServiceLoader<JadxPlugin> plugins = ServiceLoader.load(JadxPlugin.class,
                        JadXDecompiler.class.getClassLoader());
                for (JadxPlugin plugin : plugins) {
                    list.add(plugin);
                }
                return list;
            }

            @Override
            public void close() throws IOException {}
        });

        try (JadxDecompiler jadx = new JadxDecompiler(args)) {

            jadx.addCustomCodeLoader(JavaInputPlugin.load(loader -> {
                List<JavaClassReader> readers = new ArrayList<>();
                for (Map.Entry<String, byte[]> entry : classBytes.entrySet()) {
                    readers.add(loader.loadClass(entry.getValue(), entry.getKey()));
                }
                return readers;
            }));

            jadx.load();

            List<JavaClass> classes = jadx.getClasses();
            if(classes.isEmpty()) {
                throw new IllegalArgumentException("Class not found in jar: " + className);
            }
            JavaClass jc =  classes.get(0);

            //直接拿源码字符串（不写文件）
            String code = jc.getCode();
            return code != null ? code : "";
        }
    }


}
