package com.liubs.jareditor.compile;

import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/5/9
 */
public class CompilationResult {
    private final boolean success;
    private final List<String> errors;
    private final List<String> outputFiles;

    public CompilationResult(boolean success, List<String> errors, List<String> outputFiles) {
        this.success = success;
        this.errors = errors;
        this.outputFiles = outputFiles;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getOutputFiles() {
        return outputFiles;
    }
}