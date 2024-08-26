package com.liubs.jareditor.compile;



import com.liubs.jareditor.persistent.SDKSettingStorage;
import com.liubs.jareditor.util.StringUtils;

import javax.tools.*;
import java.io.File;
import java.util.*;

/**
 * 运行时编译器
 * @author Liubsyy
 * @date 2024/5/8
 */
public class MyRuntimeCompiler implements IMyCompiler {
    private JavaCompiler compiler;
    private List<String> classPaths = new ArrayList<>();
    private List<JavaFileObject> sourceCodes = new ArrayList<>();
    private String outputDirectory = "jar_edit_out";

    private String sourceVersion = "8";  // 默认目标版本
    private String targetVersion = "8";  // 默认目标版本

    public MyRuntimeCompiler() {
        this(ToolProvider.getSystemJavaCompiler());
    }
    public MyRuntimeCompiler(JavaCompiler compiler) {
        this.compiler = compiler;
    }


    @Override
    public void addClassPaths(Collection<String> classPaths) {
        this.classPaths.addAll(classPaths);
    }

    @Override
    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void addSourceCode(String className,String srcCode){
        sourceCodes.add(new JavaSourceObject(className,srcCode));
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
        if(null == this.compiler) {
            return new CompilationResult(false,
                    Collections.singletonList("Cannot find Java compiler. Make sure to use a JDK, not a JRE."), null);
        }
        List<String> options = new ArrayList<>();
        if (!classPaths.isEmpty()) {
            options.add("-classpath");
            options.add(String.join(File.pathSeparator, classPaths));
        }

        options.add("-source");
        options.add(sourceVersion);
        options.add("-target");
        options.add(targetVersion);

        options.add("-Xlint:none");

        String genDebugInfos = SDKSettingStorage.getInstance().getGenDebugInfos();
        if(StringUtils.isEmpty(genDebugInfos)) {
            options.add("-g");
        }else {
            options.add("-g:"+genDebugInfos);
        }


        DiagnosticCollector<JavaFileObject> diagnostics  = new DiagnosticCollector<>();
        StandardJavaFileManager standardFileManager = compiler.getStandardFileManager(diagnostics, null, null);

        MyJavaFileManager fileManager = new MyJavaFileManager(standardFileManager, outputDirectory);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, sourceCodes);

        boolean success = task.call();
        List<String> errors = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {

            if(Diagnostic.Kind.ERROR != diagnostic.getKind()) {
                continue;
            }

            StringBuilder sb = new StringBuilder();
            if(diagnostic.getLineNumber()>0) {
                sb.append("line:").append(diagnostic.getLineNumber());
            }
            if(diagnostic.getColumnNumber()>0) {
                sb.append(" column:").append(diagnostic.getColumnNumber());
            }
            sb.append("\n").append( diagnostic.getMessage(null));
            errors.add(sb.toString());
        }

        List<String> outputFiles = fileManager.getCompiledFiles();

        return new CompilationResult(success, errors, outputFiles);
    }



}
