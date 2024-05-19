package com.liubs.jareditor.dependency;

import java.util.List;

/**
 * @author Liubsyy
 * @date 2024/5/19
 */
public interface IDependencyHandler {

    List<String> dependentClassPaths(String jarPath,String dependencyRootPath);
}
