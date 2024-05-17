package com.liubs.jareditor.compile;

import java.util.Collection;

/**
 * @author Liubsyy
 * @date 2024/5/17
 */
 public interface IMyCompiler {

    /**
     * 增加classpath
     * @param classPaths
     */
     void addClassPaths(Collection<String> classPaths);

    /**
     * 设置输出目录
     * @param outputDirectory
     */
     void setOutputDirectory(String outputDirectory);

     void addSourceCode(String className,String srcCode);

    /**
     * 编译目标版本
     * @param targetVersion
     */
    void setTargetVersion(String targetVersion);


    /**
     * 编译结果
     * @return
     */
     CompilationResult compile();

}
