package com.liubs.jareditor.editor;

import com.liubs.jareditor.compile.ProcessCommandCompiler;

/**
 * @author Liubsyy
 * @date 2024/6/2
 */
public interface ProcessCompilerFactory {
    ProcessCommandCompiler buildCompiler(String commandHome);
}
