package com.liubs.jareditor.dependency;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/5/19
 */
public class ExtraDependencyManager {
    private static final  String DEPENDENCY_DIR = "dependency_temp";

    private List<IDependencyHandler> dependencyHandlerList = new ArrayList<>();

    public void registryNotStandardJarHandlers(){
        dependencyHandlerList.add(new SpringBootDependency());
    }

    public List<String> handleAndGetDependencyPaths(String jarPath, String tempPath){
        tempPath = tempPath+"/"+DEPENDENCY_DIR;
        List<String> result = new ArrayList<>();
        for(IDependencyHandler c: dependencyHandlerList) {
            result.addAll(c.dependentClassPaths(jarPath,tempPath));
        }

        return result;
    }

    public String filterPackage(String filePath, String packageName){
        for(IDependencyHandler c: dependencyHandlerList) {
            packageName = c.filter(filePath,packageName);
        }

        return packageName;
    }

}
