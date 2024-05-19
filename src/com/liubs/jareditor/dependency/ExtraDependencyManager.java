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
    private String jarPath;
    private String tempPath;

    public ExtraDependencyManager(String jarPath, String tempPath) {
        this.jarPath = jarPath;
        this.tempPath = tempPath+"/"+DEPENDENCY_DIR;
    }

    public void registryHandlers(){
        dependencyHandlerList.add(new SpringBootDependency());
    }

    public List<String> handleAndGetDependencyPaths(){

        List<String> result = new ArrayList<>();
        dependencyHandlerList.forEach(c->{
            result.addAll(c.dependentClassPaths(jarPath,tempPath));
        });

        return result;
    }

}
