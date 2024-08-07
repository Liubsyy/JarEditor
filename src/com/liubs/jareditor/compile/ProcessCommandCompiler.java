package com.liubs.jareditor.compile;

import com.liubs.jareditor.constant.PathConstant;
import com.liubs.jareditor.util.MyFileUtil;
import com.liubs.jareditor.util.OSUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Liubsyy
 * @date 2024/6/1
 */
public abstract class ProcessCommandCompiler implements IMyCompiler{
    protected String commandHome;
    protected List<String> classPaths = new ArrayList<>();
    protected Map<String,String> sourceCodes = new HashMap<>();
    protected String outputDirectory = "jar_edit_out";

    protected String sourceVersion;
    protected String targetVersion;

    public ProcessCommandCompiler(String javaHome) {
        this.commandHome = javaHome;
    }

    @Override
    public void addClassPaths(Collection<String> classPaths) {
//        this.classPaths.append(String.join(File.pathSeparator, classPaths));
        this.classPaths.addAll(classPaths);
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
        this.sourceVersion = targetVersion;
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
            File sourceDir = new File(outDir.getParent(), PathConstant.JAVA_SOURCE_DIR);
            if(!sourceDir.exists()) {
                sourceDir.mkdirs();
            }
            sourceDirString = sourceDir.getAbsolutePath();

            List<String> javaFilesPaths = new ArrayList<>();
            sourceCodes.forEach((className, sourceCode) -> {
                String fileName = className.replace('.', File.separatorChar) + "."+this.fileType();
                File file = new File(sourceDir.getAbsoluteFile(), fileName);
                file.getParentFile().mkdirs(); // 确保父目录存在

                try (OutputStreamWriter writer = new OutputStreamWriter(
                        new FileOutputStream(file), StandardCharsets.UTF_8)) {
                    writer.write(sourceCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                javaFilesPaths.add(file.getAbsolutePath());
            });

            // 使用ProcessBuilder
            CommandParam commandParam = new CommandParam();
            commandParam.setSourcePaths(javaFilesPaths);
            commandParam.setOutPutPath(outDir.getAbsolutePath());
            List<String> commands = buildCommand(commandParam);

            //环境变量
            Map<String, String> environment = new HashMap<>();
            putExtra(environment);


            //解决windows下命令行有最大长度限制(CreateProcess error=206)
            //将-classpath放入到环境变量CLASSPATH中
            try{
                if(OSUtil.isWindows() && commands.size() >= 2) {
                    int indexOfClassPath = commands.indexOf("-classpath");
                    if(indexOfClassPath > -1 && indexOfClassPath < commands.size()-1) {
                        String classPathStr = commands.get(indexOfClassPath+1);

                        environment.put("CLASSPATH",classPathStr);
                        commands.remove(indexOfClassPath+1);
                        commands.remove(indexOfClassPath);
                    }
                }
            }catch (Throwable t) {
                t.printStackTrace();
            }


            //执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            processBuilder.environment().putAll(environment);
            Process process = processBuilder.start();
            StringBuilder resultBuilder = new StringBuilder();

            try (BufferedReader stdOutReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                lineWhile : while ((line = stdOutReader.readLine()) != null) {
                    for(String ignoreOut : environment.keySet()) {
                        if(line.contains(ignoreOut)) {
                            continue lineWhile;
                        }
                    }
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

    protected abstract String fileType();

    protected abstract List<String> buildCommand(CommandParam commandParam);

    protected abstract void putExtra(Map<String, String> environment);
}
