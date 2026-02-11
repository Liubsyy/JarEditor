package com.liubs.jareditor.dependency;

import com.liubs.jareditor.ext.JarLikeSupports;
import com.liubs.jareditor.constant.PathConstant;
import com.liubs.jareditor.entity.SplitResult;
import com.liubs.jareditor.util.MyPathUtil;
import com.liubs.jareditor.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/5/19
 */
public class ExtraDependencyManager {

    private List<IDependencyHandler> dependencyHandlerList = new ArrayList<>();


    public String registryNotStandardJarHandlersWithPath(String packageName,String path){
        String externalPrefix = "";
        if(StringUtils.isNotEmpty(packageName)) {
            String entryPathFromJar = MyPathUtil.getEntryPathFromJar(path);
            String packagePath = packageName.replace(".", "/");
            if(null != entryPathFromJar && !entryPathFromJar.startsWith(packagePath) ) {
                // 形如 /opt/TestDemo.jar!/BOOT-INF/classes/com/liubs/web/Test.class
                int i = entryPathFromJar.indexOf(packagePath);
                if(i > -1) {
                    externalPrefix = entryPathFromJar.substring(0,i);
                    if(!externalPrefix.startsWith("/")) {
                        externalPrefix  = "/"+externalPrefix;
                    }
                    registryNotStandardJarHandlersDefault(path);
                }
            }
        }
        return externalPrefix;
    }

    public void registryNotStandardJarHandlersDefault(String path){
        SplitResult splitResult = JarLikeSupports.split(path);
        if(!splitResult.getSeparators().isEmpty()) {
            String fileExt = splitResult.getSeparators().get(0);
            if(fileExt.contains(JarLikeSupports.WAR)) {
                this.registryNotStandardJarHandler(new WarDependency());
            }else if(fileExt.contains(JarLikeSupports.JAR)){
                this.registryNotStandardJarHandler(new SpringBootDependency());
            }
        }

    }
    public void registryNotStandardJarHandler(IDependencyHandler dependencyHandler){
        dependencyHandlerList.add(dependencyHandler);
    }

    public List<String> handleAndGetDependencyPaths(String jarPath, String tempPath){
        tempPath = tempPath+"/"+ PathConstant.DEPENDENCY_DIR;
        List<String> result = new ArrayList<>();
        for(IDependencyHandler c: dependencyHandlerList) {
            result.addAll(c.dependentClassPaths(jarPath,tempPath));
        }

        return result;
    }

    public String replacePackage(String filePath, String packageName){
        for(IDependencyHandler c: dependencyHandlerList) {
            packageName = c.replacePackage(filePath,packageName);
        }

        return packageName;
    }




}
