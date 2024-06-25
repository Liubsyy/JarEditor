package com.liubs.jareditor.compile;

import com.liubs.jareditor.persistent.SDKSettingStorage;
import com.liubs.jareditor.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 调用外部javac命令进行编译
 * @author Liubsyy
 * @date 2024/5/17
 */
public class MyJavacCompiler extends ProcessCommandCompiler{

    public MyJavacCompiler(String javaHome) {
        super(javaHome);
        sourceVersion = targetVersion = "8";  // 默认目标版本
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
    protected String fileType(){
        return "java";
    }

    @Override
    protected List<String> buildCommand(CommandParam commandParam){
        // 使用ProcessBuilder启动JDK的javac编译器
        String javacPath = commandHome + "/bin/javac";  //javac路径 安装路径
        return buildCommand(javacPath,commandParam);
    }

    protected List<String> buildCommand(String javacPath,CommandParam commandParam){
        List<String> commands = new ArrayList<>();
        commands.add(javacPath);
        commands.add("-d");
        commands.add(commandParam.getOutPutPath());
        commands.add("-source");
        commands.add(sourceVersion);
        commands.add("-target");
        commands.add(targetVersion);
        commands.add( "-Xlint:none");

        String genDebugInfos = SDKSettingStorage.getInstance().getGenDebugInfos();
        if(StringUtils.isEmpty(genDebugInfos)) {
            commands.add("-g");
        }else {
            commands.add("-g:"+genDebugInfos);
        }

        if(!classPaths.isEmpty()) {
            commands.add( "-classpath");
            commands.add(String.join(File.pathSeparator, classPaths));
        }
        commands.add(String.join(" ",commandParam.getSourcePaths()));
        return commands;
    }

    @Override
    protected void putExtra(Map<String,String> environment){
        //windows下控制台javac中文乱码问题，改编码都不好使，干脆直接输出英文算了
        environment.put("JAVA_TOOL_OPTIONS", "-Dfile.encoding=UTF-8 -Duser.language=en");
    }
}
