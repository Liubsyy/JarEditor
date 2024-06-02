package com.liubs.jareditor.editor;

import com.liubs.jareditor.compile.MyJavacCompiler;
import com.liubs.jareditor.compile.MyKotlincCompiler;
import com.liubs.jareditor.compile.ProcessCommandCompiler;
import com.liubs.jareditor.util.StringUtils;

/**
 * @author Liubsyy
 * @date 2024/6/2
 */
public enum LanguageType {

    JAVAC("class","javac",  MyJavacCompiler::new),
    KOTLIN("kt","kotlinc",  MyKotlincCompiler::new),


    //add more...

    ;

    private String fileExtension;
    private String commandName;
    private ProcessCompilerFactory compilerFactory;

    LanguageType(String fileExtension, String commandName, ProcessCompilerFactory compilerFactory) {
        this.fileExtension = fileExtension;
        this.commandName = commandName;
        this.compilerFactory = compilerFactory;
    }

    public ProcessCommandCompiler buildCompiler(String commandHome){
        if(null == compilerFactory) {
            return null;
        }
        return compilerFactory.buildCompiler(commandHome);
    }


    public static LanguageType anyType(String fileExtension,String commandHome){

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
