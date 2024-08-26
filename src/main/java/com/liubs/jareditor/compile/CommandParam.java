package com.liubs.jareditor.compile;

import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/6/1
 */
public class CommandParam {

    //source file path
    private List<String> sourcePaths;

    //output dir
    private String outPutPath;

    public List<String> getSourcePaths() {
        return sourcePaths;
    }

    public void setSourcePaths(List<String> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public String getOutPutPath() {
        return outPutPath;
    }

    public void setOutPutPath(String outPutPath) {
        this.outPutPath = outPutPath;
    }


}
