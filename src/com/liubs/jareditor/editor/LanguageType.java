package com.liubs.jareditor.editor;

import com.liubs.jareditor.compile.*;
import com.liubs.jareditor.util.StringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * @author Liubsyy
 * @date 2024/6/2
 */
public enum LanguageType {

    //javac编译
    JAVAC("class",null, MyJavacCompiler::new),

    //使用jbr进行javac编译
    JAVAC_JBR("class","/jbr", MyJBRJavacCompiler::new),

    //kotlin编译
    KOTLINC("kt", null, MyKotlincCompiler::new),

    //使用SDK Default编译时，自带的kotlinc
    KOTLINC_SDK_DEFAULT("kt", "/plugins/Kotlin/kotlinc", MySDKDefaultKotlincCompiler::new),


    //add more...

    ;

    private String fileExtension;

    /**  命令子目录 */
    private String subCommandHome;

    /** 构造编译器 */
    private ProcessCompilerFactory compilerFactory;



    LanguageType(String fileExtension,String subCommandHome, ProcessCompilerFactory compilerFactory) {
        this.fileExtension = fileExtension;
        this.subCommandHome = subCommandHome;
        this.compilerFactory = compilerFactory;
    }

    public ProcessCommandCompiler buildCompiler(String commandHome){
        if(null == compilerFactory) {
            return null;
        }
        return compilerFactory.buildCompiler(commandHome);
    }


    public String getSubCommandHome() {
        return subCommandHome;
    }

    public static LanguageType matchType(String fileExtension, String commandHome){
        LanguageType target = null;
        for(LanguageType type : LanguageType.values()) {
            if(fileExtension.equals(type.fileExtension)){
                if(StringUtils.isEmpty(type.subCommandHome)){
                    target = type;
                }else {
                    if(Files.exists(Paths.get(commandHome + type.subCommandHome))){
                        target = type;
                    }
                }

            }
        }

        return target;
    }

}
