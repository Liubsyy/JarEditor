package com.liubs.jareditor.template;

import com.liubs.jareditor.util.MyPathUtil;

/**
 * @author Liubsyy
 * @date 2024/5/15
 */

public class JavaTextParser implements ITextParser{

    @Override
    public String[] parseParams(String filePath) {
        String classNameFromJar = MyPathUtil.getClassNameFromJar(filePath);
        String packageName =classNameFromJar.substring(0,classNameFromJar.lastIndexOf("."));
        String className =classNameFromJar.substring(classNameFromJar.lastIndexOf(".")+1);

        return new String[]{packageName,className};
    }
}
