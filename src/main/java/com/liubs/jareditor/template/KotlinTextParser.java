package com.liubs.jareditor.template;

import com.liubs.jareditor.dependency.ExtraDependencyManager;

/**
 * @author Liubsyy
 * @date 2024/6/3
 */
public class KotlinTextParser implements ITextParser{

    @Override
    public String[] parseParams(String filePath) {
        String[] split = filePath.split(".jar!/");
        if(split.length!=2) {
            return new String[]{""};
        }
        String replace = split[1].replace("/", ".");
        String classNameFromJar = replace.endsWith(".kt") ? replace.substring(0,replace.lastIndexOf(".kt")) : replace;
        int lastPoint = classNameFromJar.lastIndexOf(".");
        String packageName = lastPoint>0 ? classNameFromJar.substring(0,lastPoint) : "";

        ExtraDependencyManager extraDependencyManager = new ExtraDependencyManager();
        extraDependencyManager.registryNotStandardJarHandlersDefault(filePath);
        packageName = extraDependencyManager.replacePackage(filePath,packageName);

        packageName = packageName.isEmpty() ? packageName : ("package "+packageName);
        return new String[]{packageName};
    }
}
