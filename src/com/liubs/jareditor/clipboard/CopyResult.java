package com.liubs.jareditor.clipboard;


/**
 * @author Liubsyy
 * @date 2024/5/9
 */
public class CopyResult {
    private final boolean success;
    private final String error;

    public CopyResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getError() {
        return error;
    }
}