package com.liubs.jareditor.dependency;

import com.liubs.jareditor.constant.PathConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/5/19
 */
public class ExtraDependencyManager {

    private List<IDependencyHandler> dependencyHandlerList = new ArrayList<>();

    public void registryNotStandardJarHandlers(){
        dependencyHandlerList.add(new SpringBootDependency());
    }

    public List<String> handleAndGetDependencyPaths(String jarPath, String tempPath){
        tempPath = tempPath+"/"+ PathConstant.DEPENDENCY_DIR;
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
