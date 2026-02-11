package com.liubs.jareditor.structure;

import com.liubs.jareditor.constant.PathConstant;
import com.liubs.jareditor.entity.SplitResult;
import com.liubs.jareditor.ext.JarLikeSupports;
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
    public static final String KEY = PathConstant.TEMP_SUFFIX+"/"+PathConstant.NESTED_JAR_DIR_DEFAULT;



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
        String finalCurrentPath = currentPath;
        if(JarLikeSupports.FILE_EXT2.stream().noneMatch(finalCurrentPath::endsWith)) {
            currentPath = MyPathUtil.getJarFullPath(currentPath);
        }

        this.currentPath = currentPath;
        if(null != currentPath) {
            SplitResult splitResult = JarLikeSupports.split(JarLikeSupports.PATTERN_TEMP_NESTED_JAR, currentPath);
            if(!splitResult.getIndexOfs().isEmpty()) {
                int lastIndexOfKey = splitResult.getIndexOfs().get(splitResult.getIndexOfs().size()-1);
                if(lastIndexOfKey >0){
                    String tempNestedSeparator = splitResult.getSeparators().get(splitResult.getSeparators().size() - 1);
                    this.parentPath = currentPath.substring(0,lastIndexOfKey)+parseFileType(tempNestedSeparator);
                    this.originalPath = this.parentPath+"!"+currentPath.substring(lastIndexOfKey+tempNestedSeparator.length());
                }
            }
        }
    }

    private String parseFileType(String tempNestedSeparator){
        String result = tempNestedSeparator.replace(PathConstant.TEMP_SUFFIX + "/", "").replace(PathConstant.NESTED_JAR_SUFFIX, "");
        if(JarLikeSupports.FILE_EXT.contains(result)) {
            return "."+result;
        }
        return "."+JarLikeSupports.JAR;
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
