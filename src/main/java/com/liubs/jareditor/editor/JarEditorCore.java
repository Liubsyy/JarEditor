package com.liubs.jareditor.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import com.liubs.jareditor.compile.*;
import com.liubs.jareditor.dependency.ExtraDependencyManager;
import com.liubs.jareditor.dependency.NestedJarDependency;
import com.liubs.jareditor.filetree.NestedJar;
import com.liubs.jareditor.jarbuild.JarBuildResult;
import com.liubs.jareditor.jarbuild.JarBuilder;
import com.liubs.jareditor.sdk.ProjectDependency;
import com.liubs.jareditor.sdk.JavacToolProvider;
import com.liubs.jareditor.sdk.NoticeInfo;
import com.liubs.jareditor.util.JavaFileUtil;
import com.liubs.jareditor.util.MyFileUtil;
import com.liubs.jareditor.util.MyPathUtil;
import com.liubs.jareditor.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 核心功能
 * @author Liubsyy
 * @date 2024/5/9
 */
public class JarEditorCore {
    private final Project project;
    private final VirtualFile file;
    private final Editor editor;
    private NestedJar nestedJar;

    public JarEditorCore(Project project, VirtualFile file, Editor editor) {
        this.project = project;
        this.file = file;
        this.editor = editor;
        this.nestedJar = new NestedJar(file.getPath());
    }


    public void saveResource(){
        // 获取 jar 文件内文件的相对路径
        String filePath = file.getPath();

        String jarPath;
        String jarRelativePath;

        // 分离 jar 文件路径和相对路径（使用 .jar!）
        if (filePath.contains(".jar!")) {
            String[] parts = filePath.split(".jar!");
            jarPath = parts[0] + ".jar";
            jarRelativePath = parts[1].substring(1); // 去掉前导的 '/'
        } else {
            NoticeInfo.warning("File is not inside a jar archive.");
            return;
        }

        // 获取 jar 文件系统根
        VirtualFile jarRoot = VirtualFileManager.getInstance().findFileByUrl("jar://" + jarPath + "!/");
        if (jarRoot == null) {
            NoticeInfo.error("Could not find jar root.");
            return;
        }

        VirtualFile jarFile = jarRoot.findFileByRelativePath(jarRelativePath);
        if (jarFile == null) {
            NoticeInfo.error("Could not find file inside the jar.");
            return;
        }

        // 选择目标目录
        String destinationDirectory = MyPathUtil.getJarEditOutput(filePath);

        if (destinationDirectory == null || destinationDirectory.isEmpty()) {
            return;
        }

        // 将 jar 文件内的文件复制到目标目录
        try {
            String destinationPath = Paths.get(destinationDirectory, jarRelativePath).toString();
            File destinationFile = new File(destinationPath);
            destinationFile.getParentFile().mkdirs();

            Files.write(Paths.get(destinationPath),editor.getDocument().getText().getBytes(StandardCharsets.UTF_8));

            NoticeInfo.info("Save success to: " + destinationPath);

        } catch (Exception e) {
            NoticeInfo.error ( "Error copying file: " + e.getMessage());
        }

    }

    public void compileCode(String sdkHome, String targetVersion) {

        String srcCode = editor.getDocument().getText();

        // 存储类路径依赖的集合
        Set<String> classpaths = new HashSet<>();


        //非标准jar的classpath，比如SpringBoot
        ExtraDependencyManager extraDependency = new ExtraDependencyManager();
        String externalPrefix = "";
        if(nestedJar.isNested()) {
            extraDependency.registryNotStandardJarHandler(new NestedJarDependency(nestedJar));
        }else {
            externalPrefix = extraDependency.registryNotStandardJarHandlersWithPath(
                    JavaFileUtil.extractPackageName(srcCode), file.getPath());
        }
        List<String> extraPaths = extraDependency.handleAndGetDependencyPaths( MyPathUtil.getJarPathFromJar(file.getPath()), MyPathUtil.getJarEditTemp(file.getPath()));
        classpaths.addAll(extraPaths);


        //工程依赖库添加为classpath
        ProjectDependency.getDependentLib(project)
                .forEach(c-> classpaths.add(PathUtil.getLocalPath(c.getPath())));


        //编译器
        IMyCompiler myCompiler = null;
        if(StringUtils.isEmpty(sdkHome)) {
            if(!"class".equals(file.getExtension())) {
                LanguageType languageType = LanguageType.matchType(file.getExtension(),PathManager.getHomePath().replace("\\", "/"));
                if(null != languageType) {
                    myCompiler = languageType.buildCompiler(PathManager.getHomePath().replace("\\", "/"));
                }
            }

            if(null == myCompiler) {
                //默认使用运行时动态编译，基于IDEA运行时自带的JDK编译，比外部javac命令编译更快
                //比如IDEA2020.3自带JDK11, IDEA2022.2自带JDK17
                myCompiler = new MyRuntimeCompiler(JavacToolProvider.getJavaCompilerFromProjectSdk());
            }
        } else {
            LanguageType languageType = LanguageType.matchType(file.getExtension(),sdkHome);
            if(null != languageType) {
                myCompiler = languageType.buildCompiler(sdkHome);
            }

            //javac外部命令编译，为什么还用javac编译而不是全部用上面的运行时动态编译呢？
            //首先有一个前提：插件运行在IDEA自带JDK上, 比如: IDEA2020.3自带JDK11, IDEA2022.3自带JDK17
            //假如IDEA2020.3去编译JDK17的话是有问题的，因为IDEA2020.3自带JDK11,而JDK11是无法加载JDK17的类库进行动态编译的
            //这张方案看似很low，但却是比较靠谱比较稳定的方案
            //有时简单粗暴的方案恰恰是最稳妥的方案
            if(null == myCompiler) {
                myCompiler = new MyJavacCompiler(sdkHome);
            }
        }

        if(null == myCompiler) {
            NoticeInfo.error("No compiler in SDK ! ");
            return;
        }

        myCompiler.setTargetVersion(targetVersion);
        myCompiler.addClassPaths(classpaths);
        myCompiler.setOutputDirectory(MyPathUtil.getJarEditOutput(file.getPath())+externalPrefix);
        //source code
        myCompiler.addSourceCode(MyPathUtil.getClassNameFromJar(file.getPath()) ,srcCode);

        IMyCompiler finalMyCompiler = myCompiler;
        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Compiling...", true) {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    CompilationResult compilationResult = finalMyCompiler.compile();
                    if(!compilationResult.isSuccess()) {
                        NoticeInfo.error("Compile err: \n%s",compilationResult.getErrors());
                        return;
                    }
                    NoticeInfo.info("Compile successfully,output=%s",MyPathUtil.getJarEditOutput(file.getPath()));

                } catch (Exception e) {
                    e.printStackTrace();
                    NoticeInfo.error("Compile err:%s",e.getMessage());
                }

            }
        });
    }


    public void buildJar(Consumer<JarBuildResult> callBack){
        String jarEditClassPath = MyPathUtil.getJarEditOutput(file.getPath());
        if(null == jarEditClassPath){
            return;
        }
        File jarEditOutputDir = new File(jarEditClassPath);
        if(!jarEditOutputDir.exists()) {
            NoticeInfo.warning("Nothing is modified in the jar!");
            return;
        }

        String[] jarEditOutputFiles = jarEditOutputDir.list();
        if (jarEditOutputFiles == null || jarEditOutputFiles.length == 0) {
            NoticeInfo.warning("Nothing is modified in the jar!");
            return;
        }

        buildJar0(callBack);
    }

    private void buildJar0(Consumer<JarBuildResult> callBack){
        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Jar building...", true) {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    final String jarPath = MyPathUtil.getJarPathFromJar(file.getPath());
                    if(jarPath == null) {
                        return;
                    }
                    String jarEditClassPath = MyPathUtil.getJarEditOutput(file.getPath());
                    JarBuilder jarBuilder = new JarBuilder(jarEditClassPath , jarPath);
//                    JarBuildResult jarBuildResult = jarBuilder.writeJar(true);
                    JarBuildResult jarBuildResult = jarBuilder.writeJar(false);

                    if(jarBuildResult.isSuccess()) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            file.refresh(false,true);
                            VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
                        });

                        //删除临时保存的class目录
                        MyFileUtil.deleteDir(MyPathUtil.getJarEditTemp(file.getPath()));
                        NoticeInfo.info("Build jar successfully!");
                    }else {
                        NoticeInfo.error("Build jar err: \n%s",jarBuildResult.getErr());
                    }

                    if(null != callBack) {
                        callBack.accept(jarBuildResult);
                    }


                } catch (Exception e) {
                    NoticeInfo.error("Build jar err:%s",e.getMessage());
                }

            }
        });
    }


}
