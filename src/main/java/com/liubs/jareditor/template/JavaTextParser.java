package com.liubs.jareditor.template;

import com.liubs.jareditor.dependency.ExtraDependencyManager;
import com.liubs.jareditor.util.MyPathUtil;

/**
 * @author Liubsyy
 * @date 2024/5/15
 */

public class JavaTextParser implements ITextParser{

    @Override
    public String[] parseParams(String filePath) {
        String classNameFromJar = MyPathUtil.getClassNameFromJar(filePath);
        int lastPoint = classNameFromJar.lastIndexOf(".");
        String packageName = lastPoint>0 ? classNameFromJar.substring(0,lastPoint) : "";
        String className =classNameFromJar.substring(classNameFromJar.lastIndexOf(".")+1);

        ExtraDependencyManager extraDependencyManager = new ExtraDependencyManager();
        extraDependencyManager.registryNotStandardJarHandlersDefault(filePath);
        packageName = extraDependencyManager.replacePackage(filePath,packageName);

        packageName = packageName.isEmpty() ? packageName : ("package "+packageName);
        return new String[]{packageName,className};
    }
}
