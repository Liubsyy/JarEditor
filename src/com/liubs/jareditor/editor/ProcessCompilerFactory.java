package com.liubs.jareditor.editor;

import com.liubs.jareditor.compile.ProcessCommandCompiler;

/**
 * 编译器构造工程
 * @author Liubsyy
 * @date 2024/6/2
 */
public interface ProcessCompilerFactory {
    ProcessCommandCompiler buildCompiler(String commandHome);
}
