package com.liubs.jareditor.editor;

import com.liubs.jareditor.compile.MyJavacCompiler;
import com.liubs.jareditor.compile.MyKotlincCompiler;
import com.liubs.jareditor.compile.ProcessCommandCompiler;
import com.liubs.jareditor.util.OSUtil;
import com.liubs.jareditor.util.StringUtils;

import java.io.File;

/**
 * @author Liubsyy
 * @date 2024/6/2
 */
public enum LanguageType {

    JAVAC("class","javac",  MyJavacCompiler::new,null),
    KOTLIN("kt","kotlinc",  MyKotlincCompiler::new, "/plugins/Kotlin/kotlinc"),


    //add more...

    ;

    private String fileExtension;

    private String commandName;

    /** 构造编译器 */
    private ProcessCompilerFactory compilerFactory;

    /**  没有选择任何JDK时，看看内置IDEA有没有自带的编译器 */
    private String defaultCommandHome;

    LanguageType(String fileExtension, String commandName, ProcessCompilerFactory compilerFactory, String defaultCommandHome) {
        this.fileExtension = fileExtension;
        this.commandName = commandName;
        this.compilerFactory = compilerFactory;
        this.defaultCommandHome = defaultCommandHome;
    }

    public ProcessCommandCompiler buildCompiler(String commandHome){
        File file = new File(commandName);
        if(OSUtil.isWindows()) {
            if(!file.exists() && new File(commandName+".bat").exists()) {
                commandHome = commandHome+".bat";
            }
        }
        if(null == compilerFactory) {
            return null;
        }
        return compilerFactory.buildCompiler(commandHome);
    }

    public static LanguageType existDefaultCommand(String fileExtension) {
        for(LanguageType type : LanguageType.values()) {
            if(fileExtension.equals(type.fileExtension) && StringUtils.isNotEmpty(type.defaultCommandHome)){
                return type;
            }
        }
        return null;
    }

    public String getDefaultCommandHome() {
        return defaultCommandHome;
    }

    public static LanguageType anyType(String fileExtension, String commandHome){

        //Firstly match command
        for(LanguageType type : LanguageType.values()) {
            if(commandHome.contains(type.commandName)){
                return type;
            }
        }

        //then match fileType
        for(LanguageType type : LanguageType.values()) {
            if(fileExtension.equals(type.fileExtension)){
                return type;
            }
        }

        return null;
    }

}
