package com.liubs.jareditor.compile;

import com.liubs.jareditor.util.MyFileUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 调用外部javac命令进行编译
 * @author Liubsyy
 * @date 2024/5/17
 */
public class MyJavacCompiler implements IMyCompiler{

    private String javaHome;
    private StringBuilder classPaths = new StringBuilder();
    private Map<String,String> sourceCodes = new HashMap<>();
    private String outputDirectory = "jar_edit_out";

    private String sourceVersion = "8";  // 默认目标版本
    private String targetVersion = "8";  // 默认目标版本

    public MyJavacCompiler(String javaHome) {
        this.javaHome = javaHome;
    }

    @Override
    public void addClassPaths(Collection<String> classPaths) {
        this.classPaths.append(String.join(File.pathSeparator, classPaths));
    }

    @Override
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void addSourceCode(String className, String srcCode) {
        sourceCodes.put(className,srcCode);
    }

    @Override
    public void setTargetVersion(String targetVersion) {
        if("1.1".equals(targetVersion)){
            this.sourceVersion = "1.3"; // -source 1.1已经不支持了
        }else {
            this.sourceVersion = targetVersion;
        }
        this.targetVersion = targetVersion;
    }

    @Override
    public CompilationResult compile() {


        String sourceDirString = null;
        try{
            //输出目录创建
            Path outputDir = Paths.get(outputDirectory);
            if (Files.notExists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            File outDir = new File(outputDirectory);

            //.java文件创建临时目录
            File sourceDir = new File(outDir.getParent(), "jar_edit_java_source");
            if(!sourceDir.exists()) {
                sourceDir.mkdirs();
            }
            sourceDirString = sourceDir.getAbsolutePath();

            List<String> javaFilesPaths = new ArrayList<>();
            sourceCodes.forEach((className, sourceCode) -> {
                String fileName = className.replace('.', File.separatorChar) + ".java";
                File file = new File(sourceDir.getAbsoluteFile(), fileName);
                file.getParentFile().mkdirs(); // 确保父目录存在
                // 使用 OutputStreamWriter 并指定 UTF-8 编码
                try (OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    writer.write(sourceCode); // 写入代码到文件
                } catch (Exception e) {
                    e.printStackTrace();
                }
                javaFilesPaths.add(file.getAbsolutePath());
            });


            // 使用ProcessBuilder启动JDK的javac编译器
            String javacPath = javaHome + "/bin/javac";  //javac路径 安装路径
            String classPath = classPaths.toString();
            List<String> commands = new ArrayList<>();
            commands.add("-encoding");
            commands.add("UTF-8");
            commands.add(javacPath);
            commands.add("-d");
            commands.add(outDir.getAbsolutePath());
            commands.add("-source");
            commands.add(sourceVersion);
            commands.add("-target");
            commands.add(targetVersion);
            commands.add( "-Xlint:none");
            commands.add( "-g");
            if(!classPath.isEmpty()) {
                commands.add( "-classpath");
                commands.add(String.join(File.pathSeparator, classPaths));
            }
            commands.add(String.join(" ",javaFilesPaths));

            ProcessBuilder processBuilder = new ProcessBuilder(commands);

            Process process = processBuilder.start();
            StringBuilder resultBuilder = new StringBuilder();

            try (BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = stdOutReader.readLine()) != null) {
                    resultBuilder.append(line);
                }
            }
            int result = process.waitFor();
            if (result == 0) {
                return new CompilationResult(true,null,null);
            } else {
                return new CompilationResult(false,Collections.singletonList(resultBuilder.toString()),null);
            }

        }catch (Throwable throwable) {
            throwable.printStackTrace();
            return new CompilationResult(false,Collections.singletonList(throwable.getMessage()),null);
        }finally {
            //删除java文件
            try{
                if(null != sourceDirString) {
                    MyFileUtil.deleteDir(sourceDirString);
                }
            }catch (Throwable fTh) {}

        }

    }


}
