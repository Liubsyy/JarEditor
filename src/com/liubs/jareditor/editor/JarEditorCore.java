package com.liubs.jareditor.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.PathUtil;
import com.liubs.jareditor.compile.CompilationResult;
import com.liubs.jareditor.compile.IMyCompiler;
import com.liubs.jareditor.compile.MyJavacCompiler;
import com.liubs.jareditor.compile.MyRuntimeCompiler;
import com.liubs.jareditor.dependency.ExtraDependencyManager;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 核心功能
 * @author Liubsyy
 * @date 2024/5/9
 */
public class JarEditorCore {
    private final Project project;
    private final VirtualFile file;
    private final Editor editor;

    public JarEditorCore(Project project, VirtualFile file, Editor editor) {
        this.project = project;
        this.file = file;
        this.editor = editor;
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

    public void compileJavaCode(String javaHome,String targetVersion) {

        // 存储类路径依赖的集合
        Set<String> classpaths = new HashSet<>();

        ExtraDependencyManager extraDependency = new ExtraDependencyManager();

        String srcCode = editor.getDocument().getText();
        String packageName = JavaFileUtil.extractPackageName(srcCode);
        String externalPrefix = "";
        if(StringUtils.isNotEmpty(packageName)) {
            String entryPathFromJar = MyPathUtil.getEntryPathFromJar(file.getPath());
            String packagePath = packageName.replace(".", "/");
            if(null != entryPathFromJar && !entryPathFromJar.startsWith(packagePath) ) {
                // /opt/TestDemo.jar!/BOOT-INF/classes/com/liubs/web/Test.class
                int i = entryPathFromJar.indexOf(packagePath);
                if(i > -1) {
                    externalPrefix = entryPathFromJar.substring(0,i);
                    if(!externalPrefix.startsWith("/")) {
                        externalPrefix  = "/"+externalPrefix;
                    }
                    extraDependency.registryNotStandardJarHandlers();
                }
            }
        }
        List<String> extraPaths = extraDependency.handleAndGetDependencyPaths( MyPathUtil.getJarPathFromJar(file.getPath()), MyPathUtil.getJarEditTemp(file.getPath()));
        classpaths.addAll(extraPaths);

        ProjectDependency.getDependentLib(project).forEach(c-> classpaths.add(PathUtil.getLocalPath(c.getPath())));


        //编译器
        IMyCompiler myCompiler;
        if(StringUtils.isEmpty(javaHome)) {
            //默认使用运行时动态编译，基于IDEA运行时自带的JDK编译，相对javac命令编译更快
            //比如IDEA2020.3自带JDK11, IDEA2022.3自带JDK17
            myCompiler = new MyRuntimeCompiler(JavacToolProvider.getJavaCompilerFromProjectSdk());
        }else {
            //javac外部命令编译，为什么还用javac编译而不是全部用上面的运行时动态编译呢？
            //首先有一个前提：插件运行在IDEA自带JDK上, 比如: IDEA2020.3自带JDK11, IDEA2022.3自带JDK17
            //假如IDEA2020.3去编译JDK17的话是有问题的，因为IDEA2020.3自带JDK11,而JDK11是无法加载JDK17的类库进行动态编译的
            //这张方案看似很low，但却是比较靠谱比较稳定的方案
            //有时简单粗暴的方案恰恰是最稳妥的方案
            myCompiler = new MyJavacCompiler(javaHome);
        }
        myCompiler.setTargetVersion(targetVersion);
        myCompiler.addClassPaths(classpaths);
        myCompiler.setOutputDirectory(MyPathUtil.getJarEditOutput(file.getPath())+externalPrefix);
        //source code
        myCompiler.addSourceCode(MyPathUtil.getClassNameFromJar(file.getPath()) ,srcCode);

        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Compiling...", false) {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    CompilationResult compilationResult = myCompiler.compile();
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


    public void buildJar(){
        String jarEditClassPath = MyPathUtil.getJarEditOutput(file.getPath());
        if(null == jarEditClassPath){
            return;
        }
        if(!Files.exists(Paths.get(jarEditClassPath))) {
            NoticeInfo.warning("Nothing is modified in the jar!");
            return;
        }
        buildJar0();
    }

    private void buildJar0(){
        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Jar building...", false) {
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

                    if(!jarBuildResult.isSuccess()) {
                        NoticeInfo.error("Build jar err: \n%s",jarBuildResult.getErr());
                        return;
                    }

                    //Reload from Disk
                    ArrayList<VirtualFile> filesToRefresh = new ArrayList<>();
                    ProjectDependency.getDependentLib(project).forEach(c->{
                        if(file.getPath().contains(jarPath)){
                            filesToRefresh.add(file);
                        }
                    });
                    if(!filesToRefresh.isEmpty()) {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            for (VirtualFile refreshFile : filesToRefresh) {
                                refreshFile.refresh(false,true);
                            }
                            // 刷新整个虚拟文件系统
                            VirtualFileManager.getInstance().refreshWithoutFileWatcher(true);
                        });
                    }

                    //删除临时保存的class目录
                    MyFileUtil.deleteDir(MyPathUtil.getJarEditTemp(file.getPath()));

                    NoticeInfo.info("Build jar successfully!");

                } catch (Exception e) {
                    NoticeInfo.error("Build jar err:%s",e.getMessage());
                }

            }
        });
    }


}
