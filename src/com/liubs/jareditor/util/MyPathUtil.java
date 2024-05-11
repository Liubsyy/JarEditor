package com.liubs.jareditor.util;

/**
 * @author Liubsyy
 * @date 2024/5/9
 */
public class MyPathUtil {

    public static final String JAR_EDIT_CLASS_PATH = "jar_edit_out";

    /**
     * /path/a.jar!/com/liubs/A.class转换成com.liubs.A
     * @param classNameInJar
     * @return
     */
    public static String getClassNameFromJar(String classNameInJar) {
        String[] split = classNameInJar.split(".jar!/");
        if(split.length!=2) {
            return null;
        }
        return split[1].replace("/",".").replace(".class","");
    }

    public static String getJarPathFromJar(String classNameInJar) {
        String[] split = classNameInJar.split(".jar!/");
        if(split.length!=2) {
            return null;
        }
        return split[0]+".jar";
    }

    /**
     * find jar path
     * /path/a.jar!/com/liubs/A.class转换成/path
     * @return
     */
    private static String getJarRootPath(String classNameInJar) {
        String[] split = classNameInJar.split(".jar!/");
        if(split.length!=2) {
            return null;
        }
        return split[0].substring(0, split[0].lastIndexOf("/"));
    }

    public static String getJarEditClassPath(String classNameInJar){
        String jarRootPath = getJarRootPath(classNameInJar);
        if(null == jarRootPath) {
            return null;
        }
        return jarRootPath+"/"+JAR_EDIT_CLASS_PATH;
    }


}
