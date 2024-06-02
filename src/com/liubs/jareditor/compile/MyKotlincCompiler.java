package com.liubs.jareditor.compile;

import com.liubs.jareditor.util.OSUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Liubsyy
 * @date 2024/6/1
 */
public class MyKotlincCompiler extends ProcessCommandCompiler{

    public MyKotlincCompiler(String javaHome) {
        super(javaHome);
    }

    @Override
    protected String fileType() {
        return "kt";
    }

    @Override
    public void addSourceCode(String className, String srcCode) {
        if(className.endsWith(".kt")) {
            className = className.replace(".kt" ,"");
        }
        super.addSourceCode(className, srcCode);
    }

    /**
     * Kotlin supported versions: [1.6, 1.8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19...]
     * @param targetVersion
     */
    @Override
    public void setTargetVersion(String targetVersion) {
        super.setTargetVersion(targetVersion);
        if("6".equals(targetVersion)) {
            this.targetVersion = "1.6";
        }
        if("8".equals(targetVersion)) {
            this.targetVersion = "1.8";
        }
    }

    @Override
    protected List<String> buildCommand(CommandParam commandParam) {
        // kotlinc
        String kotlinc = commandHome + "/bin/kotlinc";  //kotlinc

        File file = new File(kotlinc);
        if(OSUtil.isWindows()) {
            if(!file.exists() && new File(kotlinc+".bat").exists()) {
                kotlinc = kotlinc+".bat";
            }
        }

        String classPath = classPaths.toString();
        List<String> commands = new ArrayList<>();
        commands.add(kotlinc);
        commands.add("-d");
        commands.add(commandParam.getOutPutPath());
        commands.add("-jvm-target");
        commands.add(targetVersion);
//        commands.add( "-Xlint:none");
//        commands.add( "-g");
        if(!classPath.isEmpty()) {
            commands.add( "-classpath");
            commands.add(String.join(File.pathSeparator, classPaths));
        }
        commands.add(String.join(" ",commandParam.getSourcePaths()));
        return commands;
    }

    @Override
    protected void putExtra(Map<String, String> environment) {

    }
}
