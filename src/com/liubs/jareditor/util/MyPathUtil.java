package com.liubs.jareditor.util;

import com.liubs.jareditor.constant.PathConstant;

/**
 * @author Liubsyy
 * @date 2024/5/9
 */
public class MyPathUtil {


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
        String classFullName = split[1];
        int lastIndex = classFullName.lastIndexOf('.');
        if(lastIndex > 0) {
            classFullName = classFullName.substring(0,lastIndex);
        }
        return classFullName.replace("/", ".");
//        String replace = split[1].replace("/", ".");
//        return replace.endsWith(".class") ? replace.substring(0,replace.lastIndexOf(".class")) : replace;
    }

    public static String getJarPathFromJar(String classNameInJar) {
        String[] split = classNameInJar.split(".jar!/");
        if(split.length!=2) {
            return null;
        }
        return split[0]+".jar";
    }


    public static String getEntryPathFromJar(String fullPath) {
        String[] split = fullPath.split(".jar!/");
        if(split.length!=2) {
            return null;
        }
        return split[1];
    }

    /**
     * /path/a.jar!/com/liubs/AAA.class===>AAA
     * @param classNameInJar
     * @return
     */
    public static String getJarNameFromClassJar(String classNameInJar) {
        String[] split = classNameInJar.split(".jar!/");
        if(split.length!=2) {
            return null;
        }
        int i = split[0].lastIndexOf("/");
        if(i > -1) {
            return split[0].substring(i+1);
        }
        return split[0];
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

    /**
     * 获取编译输出目录
     * /path/a.jar!/com/liubs/A.class转换成 /path/a_temp/jar_edit_out
     * @param classNameInJar
     * @return
     */
    public static String getJarEditOutput(String classNameInJar){
        String[] split = classNameInJar.split(".jar!/");
        if(split.length!=2) {
            return null;
        }

        return split[0]+"_temp/"+ PathConstant.JAR_EDIT_CLASS_PATH;
    }

    public static String getCLIPBOARD_TO_FILE(String classNameInJar){
        if(classNameInJar.endsWith(".jar!/")) {
            return classNameInJar.replace(".jar!/","")+"_temp/"+PathConstant.CLIPBOARD_TO_FILE;
        }
        String[] split = classNameInJar.split(".jar!/");
        if(split.length!=2) {
            return null;
        }

        return split[0]+"_temp/"+PathConstant.CLIPBOARD_TO_FILE;
    }
    public static String getFILE_TO_CLIPBOARD(String classNameInJar){
        if(classNameInJar.endsWith(".jar!/")) {
            return classNameInJar.replace(".jar!/","")+"_temp/"+PathConstant.FILE_TO_CLIPBOARD;
        }
        String[] split = classNameInJar.split(".jar!/");
        if(split.length!=2) {
            return null;
        }

        return split[0]+"_temp/"+PathConstant.FILE_TO_CLIPBOARD;
    }


    public static String getJarEditTemp(String classNameInJar){
        String[] split = classNameInJar.split(".jar!/");
        if(split.length!=2) {
            return null;
        }

        return split[0]+"_temp";
    }


    public static boolean isSourceJar(String classNameInJar) {
        return null != classNameInJar && classNameInJar.contains("-sources.jar!");
    }


}
