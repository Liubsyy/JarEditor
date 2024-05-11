package com.liubs.jareditor.jarbuild;

/**
 * @author Liubsyy
 * @date 2024/5/9
 */
public class JarBuildResult {
    private final boolean success;
    private final String err;

    public JarBuildResult(boolean success, String err) {
        this.success = success;
        this.err = err;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErr() {
        return err;
    }

}
