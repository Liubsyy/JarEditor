package com.liubs.jareditor.filetree;

import com.liubs.jareditor.constant.PathConstant;
import com.liubs.jareditor.util.MyPathUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 嵌套jar：可支持多层嵌套
 *
 * /path/a.jar
 *      /class
 *      /lib/b.jar
 *           /class2
 *           /lib/c1.jar
 *           /lib/c2.jar
 *
 * @author Liubsyy
 * @date 2024/10/13
 */
public class NestedJar {
    private static final String KEY = PathConstant.TEMP_SUFFIX+"/"+PathConstant.NESTED_JAR_DIR;

    /**
     * 父节点路径
     * 比如 /path/a.jar内嵌套/path/a.jar!/lib/b.jar
     * 那么这里的b.jar的parentPath就是 /path/a.jar
     */
    private String parentPath;

    /**
     * 嵌套jar当前路径
     */
    private String currentPath;

    /**
     * 原路径，形如/path/a.jar!/lib/b.jar
     */
    private String originalPath;


    public NestedJar(String currentPath) {
        if(!currentPath.endsWith(".jar")){
            currentPath = MyPathUtil.getJarFullPath(currentPath);
        }
        this.currentPath = currentPath;
        if(null != currentPath) {
            int lastIndexOfKey = currentPath.lastIndexOf(KEY);
            if(lastIndexOfKey >0){
                this.parentPath = currentPath.substring(0,lastIndexOfKey)+".jar";
                this.originalPath = this.parentPath+"!"+currentPath.substring(lastIndexOfKey+KEY.length());
            }
        }
    }

    /**
     * 依次列出多层嵌套jar的每一层jar
     * @return
     */
    public List<NestedJar> listDepthJars(){
        List<NestedJar> result = new ArrayList<>();
        String currentPath;
        NestedJar nestedJar = this;
        do{
            result.add(nestedJar);
            currentPath = nestedJar.getParentPath();
            if(null != nestedJar.getParentPath()) {
                nestedJar = new NestedJar(nestedJar.getParentPath());
            }
        }while(null != currentPath);
        return result;
    }


    public boolean isNested(){
        return parentPath != null;
    }

    public String getParentPath() {
        return parentPath;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public String getOriginalPath() {
        return originalPath;
    }
}
