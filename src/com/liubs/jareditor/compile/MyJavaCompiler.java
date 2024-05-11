package com.liubs.jareditor.compile;



import javax.tools.*;
import java.io.File;
import java.util.*;

/**
 * 运行时编译器
 */
public class MyJavaCompiler {
    private JavaCompiler compiler;
    private List<String> classPaths;
    private List<JavaFileObject> sourceCodes;
    private String outputDirectory = "jar_edit_out";

    private String targetVersion = "8";  // 默认目标版本

    public MyJavaCompiler() {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        this.sourceCodes = new ArrayList<>();
        this.classPaths = new ArrayList<>();
    }

    public void setCompiler(JavaCompiler compiler) {
        this.compiler = compiler;
    }

    public void addClassPaths(Collection<String> classPaths) {
        this.classPaths.addAll(classPaths);
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public void addSourceCode(String className,String srcCode){
        sourceCodes.add(new JavaSourceObject(className,srcCode));
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }



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
        options.add(targetVersion);
        options.add("-target");
        options.add(targetVersion);

        options.add("-Xlint:unchecked");
        options.add("-g");

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
