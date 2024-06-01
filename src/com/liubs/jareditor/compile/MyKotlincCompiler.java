package com.liubs.jareditor.compile;

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
    protected List<String> buildCommand(CommandParam commandParam) {
        // kotlinc
        String javacPath = commandHome + "/bin/kotlinc";  //kotlinc
        String classPath = classPaths.toString();
        List<String> commands = new ArrayList<>();
        commands.add(javacPath);
        commands.add("-d");
        commands.add(commandParam.getOutPutPath());
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
        commands.add(String.join(" ",commandParam.getSourcePaths()));
        return commands;
    }

    @Override
    protected void putExtra(Map<String, String> environment) {

    }
}
