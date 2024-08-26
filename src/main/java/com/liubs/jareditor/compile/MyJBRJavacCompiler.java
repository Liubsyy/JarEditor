package com.liubs.jareditor.compile;

import com.liubs.jareditor.editor.LanguageType;
import com.liubs.jareditor.util.MyFileUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * 使用JBR进行编译
 * 可用于编译：依赖SDK的class，IntelliJ插件
 * @author Liubsyy
 * @date 2024/6/25
 */
public class MyJBRJavacCompiler extends MyJavacCompiler{
    public MyJBRJavacCompiler(String javaHome) {
        super(javaHome);
    }

    @Override
    protected List<String> buildCommand(CommandParam commandParam) {

        //添加lib和plugins的jar作为依赖
        if(Files.exists(Paths.get(commandHome+"/lib"))) {
            classPaths.addAll(MyFileUtil.findAllJars(commandHome+"/lib"));
        }
        if(Files.exists(Paths.get(commandHome+"/plugins"))) {
            classPaths.addAll(MyFileUtil.findAllJars(commandHome+"/plugins"));
        }

        commandHome = commandHome + LanguageType.JAVAC_JBR.getSubCommandHome();

        //这里遍历搜索bin/javac，mac和windows目录结构还不一样，懒得去枚举了
        List<String> javacCommand = MyFileUtil
                .searchFile(commandHome, path -> path.toString().replace("\\", "/").contains("/bin/javac"));
        if(javacCommand.isEmpty()) {
            throw new RuntimeException("javac not found in : "+commandHome);
        }

        return super.buildCommand(javacCommand.get(0),commandParam);
    }
}
